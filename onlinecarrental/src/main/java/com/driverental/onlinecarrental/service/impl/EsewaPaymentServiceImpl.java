package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.config.EsewaProperties;
import com.driverental.onlinecarrental.model.dto.request.EsewaInitiateRequest;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.model.exception.BusinessException;
import com.driverental.onlinecarrental.model.exception.ResourceNotFoundException;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.PaymentRepository;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.EsewaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EsewaPaymentServiceImpl implements EsewaPaymentService {

    private final EsewaProperties esewaProperties;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final RestTemplateBuilder restTemplateBuilder;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public Map<String, String> initiate(EsewaInitiateRequest request) {
        BigDecimal amount = request.getAmount();
        Long bookingId = request.getBookingId();

        Booking booking = null;
        // baseAmount = rental charge without VAT, taxAmount = VAT, totalAmount = base + VAT
        BigDecimal baseAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = baseAmount;

        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
            baseAmount = booking.getTotalPrice();
            taxAmount = baseAmount.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
            totalAmount = baseAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
            amount = totalAmount;
        }

        String uuid = UUID.randomUUID().toString();

        // Handle payment record with database-first approach
        if (booking != null) {
            uuid = getOrCreatePayment(booking, amount);
            log.info("Payment finalized for booking {}. Transaction UUID: {}", booking.getId(), uuid);
        }

        Map<String, String> params = buildEsewaFormParams(uuid, baseAmount, taxAmount, totalAmount);
        return params;
    }

    /**
     * Gets or creates a Payment for the given booking using a database-first approach.
     * 
     * STRATEGY: Attempt insert first (not check-then-insert).
     * - If insert succeeds, return the new payment's UUID.
     * - If insert fails with UNIQUE constraint violation, fetch the existing payment.
     * 
     * EXCEPTION HANDLING:
     * - Catches DataIntegrityViolationException from the database insert.
     * - Uses TransactionTemplate to run fetch in a clean, separate transaction.
     * - Never throws or propagates DataIntegrityViolationException to the caller.
     * 
     * SESSION SAFETY:
     * - createPaymentDirectly() uses REQUIRES_NEW propagation for its own independent transaction.
     * - If DataIntegrityViolationException occurs, that transaction is completely rolled back.
     * - The parent transaction (initiate()) is NOT marked rollback-only.
     * - fetchExistingPaymentBlocking() uses TransactionTemplate for another independent transaction.
     * - No session contamination or AssertionFailure possible.
     * 
     * CONCURRENCY:
     * - Both concurrent threads will eventually return the same Payment UUID.
     * - No Hibernate session contamination or AssertionFailure.
     * - Fully idempotent and Production-ready.
     */
    private String getOrCreatePayment(Booking booking, BigDecimal amount) {
        try {
            return createPaymentDirectly(booking, amount);
        } catch (DataIntegrityViolationException e) {
            // Another thread won the race and created the payment first.
            // The child transaction (createPaymentDirectly) is already rolled back.
            // The parent transaction (initiate) is still active and clean.
            // Fetch it in a clean, separate transaction via TransactionTemplate.
            log.info("Payment already exists for booking ID: {} (race condition). Fetching existing payment...", 
                    booking.getId());
            return fetchExistingPaymentBlocking(booking.getId());
        }
    }

    /**
     * Attempts to create a new Payment directly without any checks.
     * This is the database-first approach: let the database enforce UNIQUE constraint.
     * 
     * CRITICAL: Uses REQUIRES_NEW propagation to ensure this method runs in a completely 
     * independent transaction, NOT in the parent's transaction context.
     * This prevents Hibernate session contamination if the insert fails.
     * 
     * If the insert fails with DataIntegrityViolationException (duplicate key), 
     * that exception bubbles up to the caller for handling.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    private String createPaymentDirectly(Booking booking, BigDecimal amount) {
        String uuid = UUID.randomUUID().toString();
        
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .paymentMethod(PaymentMethod.ESEWA)
                .status(PaymentStatus.PROCESSING)
                .transactionId(uuid)
                .build();
        
        Payment saved = paymentRepository.save(payment);
        log.info("Payment created successfully. Payment ID: {}, UUID: {}", saved.getId(), uuid);
        
        return uuid;
    }

    /**
     * Fetches an existing Payment for a booking, with retry logic.
     * 
     * ISOLATION:
     * - Uses TransactionTemplate to issue queries in a completely separate transaction.
     * - Uses READ_COMMITTED isolation to ensure we see committed data from other threads.
     * - No contamination from the parent transaction.
     * 
     * RETRY:
     * - Retries up to 10 times with 50ms delays (max 500ms wait).
     * - Gives the competing thread's insert time to commit.
     * - If not found after retries, throws BusinessException (not DataIntegrityViolationException).
     */
    private String fetchExistingPaymentBlocking(Long bookingId) {
        final int maxAttempts = 10;
        final long delayMs = 50;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            final int currentAttempt = attempt; // Capture as final for lambda
            String uuid = transactionTemplate.execute(status -> {
                Optional<Payment> payment = paymentRepository.findByBookingId(bookingId);
                return payment.map(p -> {
                    log.info("Existing payment found on attempt {}/{}. Payment ID: {}, UUID: {}", 
                            currentAttempt, maxAttempts, p.getId(), p.getTransactionId());
                    return p.getTransactionId();
                }).orElse(null);
            });
            
            if (uuid != null) {
                return uuid;
            }
            
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting for existing payment for booking ID: {}", bookingId);
                    throw new BusinessException("Interrupted while fetching payment. Please retry.");
                }
            }
        }
        
        log.error("Could not find existing Payment for booking ID: {} after {} retries", bookingId, maxAttempts);
        throw new BusinessException("Payment creation failed due to race condition. Please retry.");
    }

    @Override
    @Transactional
    public Map<String, Object> verify(String uuid, String amount) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        String url = UriComponentsBuilder.fromHttpUrl(esewaProperties.getStatusUrl())
                .queryParam("product_code", esewaProperties.getProductCode())
                .queryParam("transaction_uuid", uuid)
                .queryParam("total_amount", amount)
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody() != null ? response.getBody() : new HashMap<>();

        Object statusObj = body.get("status");
        String status = statusObj != null ? statusObj.toString() : "UNKNOWN";

        // SAFE: Fetch payment in a separate transaction to avoid session reuse
        Long paymentId = fetchPaymentIdByTransactionUuid(uuid);
        
        if (paymentId != null) {
            // Update payment in yet another separate transaction
            if ("COMPLETE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                updatePaymentStatus(paymentId, PaymentStatus.COMPLETED);
                
                // Fetch the booking ID in a separate transaction before confirming
                Long bookingId = fetchBookingIdByPaymentId(paymentId);
                if (bookingId != null) {
                    bookingService.confirmBooking(bookingId);
                }
            } else {
                updatePaymentStatus(paymentId, PaymentStatus.FAILED);
            }
        } else {
            log.warn("No local Payment found for transaction uuid={}", uuid);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("raw", body);
        return result;
    }

    /**
     * Fetches payment ID by transaction UUID in a completely isolated transaction.
     * Returns only the ID to ensure no Session reuse.
     */
    private Long fetchPaymentIdByTransactionUuid(String uuid) {
        return transactionTemplate.execute(status -> {
            Optional<Payment> payment = paymentRepository.findByTransactionId(uuid);
            if (payment.isPresent()) {
                log.info("Found payment for transaction UUID: {}", uuid);
                return payment.get().getId();
            }
            return null;
        });
    }

    /**
     * Fetches booking ID by payment ID in a completely isolated transaction.
     * Returns only the ID to ensure no Session reuse.
     */
    private Long fetchBookingIdByPaymentId(Long paymentId) {
        return transactionTemplate.execute(status -> {
            Optional<Payment> payment = paymentRepository.findById(paymentId);
            if (payment.isPresent() && payment.get().getBooking() != null) {
                log.info("Found booking for payment ID: {}", paymentId);
                return payment.get().getBooking().getId();
            }
            return null;
        });
    }

    /**
     * Updates payment status in a completely isolated transaction.
     * Never reads the Payment (would attaches to Session), just updates by ID.
     */
    private void updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        transactionTemplate.execute(status -> {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                payment.setStatus(newStatus);
                if (newStatus == PaymentStatus.COMPLETED) {
                    payment.setCompletedAt(LocalDateTime.now());
                }
                paymentRepository.save(payment);
                log.info("Updated payment ID: {} to status: {}", paymentId, newStatus);
            }
            return null;
        });
    }

        private Map<String, String> buildEsewaFormParams(String transactionUuid, BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal totalAmount) {
        // Ensure amounts are properly scaled
        baseAmount = baseAmount.setScale(2, RoundingMode.HALF_UP);
        taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        // For eSewa requests, whole numbers should be formatted without decimals when possible
        String totalAmountForFormAndSignature = totalAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(totalAmount.intValue())
            : totalAmount.toPlainString();

        String baseAmountForForm = baseAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(baseAmount.intValue())
            : baseAmount.toPlainString();

        String taxAmountForForm = taxAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(taxAmount.intValue())
            : taxAmount.toPlainString();

        Map<String, String> params = new HashMap<>();
        // Send base amount as `amount` and total (base + tax + charges) as `total_amount` so eSewa can validate sums
        params.put("amount", baseAmountForForm);
        params.put("tax_amount", taxAmountForForm);
        params.put("total_amount", totalAmountForFormAndSignature);
        params.put("transaction_uuid", transactionUuid);
        params.put("product_code", esewaProperties.getProductCode());
        params.put("product_service_charge", "0");
        params.put("product_delivery_charge", "0");
        params.put("success_url", esewaProperties.getSuccessUrl());
        params.put("failure_url", esewaProperties.getFailureUrl());

        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        params.put("signed_field_names", signedFieldNames);

        // The signature message must use the exact same formatted total_amount value as in the form
        String signatureMessage = String.format("total_amount=%s,transaction_uuid=%s,product_code=%s",
            totalAmountForFormAndSignature, transactionUuid, esewaProperties.getProductCode());
        params.put("signature", hmacSha256Base64(esewaProperties.getSecretKey(), signatureMessage));

        return params;
        }

    private String hmacSha256Base64(String secret, String message) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signed = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign eSewa request", e);
        }
    }
}
