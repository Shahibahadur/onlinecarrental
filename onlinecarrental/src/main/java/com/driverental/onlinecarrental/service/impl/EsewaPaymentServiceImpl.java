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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Map<String, String> initiate(EsewaInitiateRequest request) {
        BigDecimal amount = request.getAmount();
        Long bookingId = request.getBookingId();

        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
            amount = booking.getTotalPrice();
        }

        String uuid = UUID.randomUUID().toString();

        // Handle payment record with proper transaction management
        if (booking != null) {
            // First, check if payment already exists
            Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
            
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                
                // If already completed, reject
                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                    throw new BusinessException("Payment already completed for this booking");
                }
                
                log.info("Updating existing payment {} for booking {}", payment.getId(), booking.getId());
                
                // Reuse existing transaction UUID for retry attempts
                if (payment.getTransactionId() != null) {
                    uuid = payment.getTransactionId();
                } else {
                    payment.setTransactionId(uuid);
                }
                
                payment.setAmount(amount);
                payment.setPaymentMethod(PaymentMethod.ESEWA);
                payment.setStatus(PaymentStatus.PROCESSING);
                paymentRepository.save(payment);
                
                log.info("Payment updated successfully. UUID: {}", uuid);
            } else {
                // Try to create new payment, but handle race condition
                uuid = createPaymentWithRaceConditionHandling(booking, amount);
                log.info("Payment resolved for booking {}. UUID: {}", booking.getId(), uuid);
            }
        }

        Map<String, String> params = buildEsewaFormParams(uuid, amount);
        return params;
    }

    /**
     * Creates a new payment record with race condition handling.
     * If another thread creates the payment concurrently, we fetch and use theirs.
     * Runs in the parent transaction context.
     */
    private String createPaymentWithRaceConditionHandling(Booking booking, BigDecimal amount) {
        try {
            // Attempt to create new payment
            log.info("Creating new payment for booking {}", booking.getId());
            
            String uuid = UUID.randomUUID().toString();
            
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod(PaymentMethod.ESEWA)
                    .status(PaymentStatus.PROCESSING)
                    .transactionId(uuid)
                    .build();
            
            Payment saved = paymentRepository.save(payment);
            log.info("Payment created successfully. ID: {}, UUID: {}", saved.getId(), uuid);
            
            return uuid;
        } catch (DataIntegrityViolationException e) {
            // Race condition: another thread created payment first
            log.warn("Race condition detected for booking {}. Fetching existing payment created by another thread.", 
                    booking.getId());
            
            // Clear the corrupted session state to prevent Hibernate assertion failures
            entityManager.clear();
            
            // Fetch the payment that was created by the winning thread
            Optional<Payment> raceWinnerPayment = paymentRepository.findByBookingId(booking.getId());
            
            if (raceWinnerPayment.isPresent()) {
                Payment payment = raceWinnerPayment.get();
                log.info("Successfully recovered from race condition. Using payment ID: {}, UUID: {}", 
                        payment.getId(), payment.getTransactionId());
                
                // Return the existing transaction UUID
                return payment.getTransactionId() != null ? payment.getTransactionId() : UUID.randomUUID().toString();
            } else {
                // This shouldn't happen, but handle it gracefully
                log.error("Race condition occurred but cannot find the created payment for booking {}", booking.getId());
                throw new BusinessException("Failed to create payment. Please try again.");
            }
        }
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

        Payment payment = paymentRepository.findByTransactionId(uuid).orElse(null);

        if (payment != null) {
            if ("COMPLETE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCompletedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                if (payment.getBooking() != null) {
                    bookingService.confirmBooking(payment.getBooking().getId());
                }
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        } else {
            log.warn("No local Payment found for transaction uuid={}", uuid);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("raw", body);
        return result;
    }

    private Map<String, String> buildEsewaFormParams(String transactionUuid, BigDecimal totalAmount) {
        // Ensure the amount is properly scaled
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        
        // For eSewa requests, whole numbers should be formatted without decimals
        // (e.g., "100" instead of "100.00") and decimals should be plain strings
        String totalAmountForFormAndSignature = totalAmount.stripTrailingZeros().scale() <= 0
                ? String.valueOf(totalAmount.intValue())
                : totalAmount.toPlainString();

        // Calculate amount breakdown (all charges are zero for test/basic implementation)
        BigDecimal taxAmount = new BigDecimal("0.00");
        BigDecimal serviceCharge = new BigDecimal("0.00");
        BigDecimal deliveryCharge = new BigDecimal("0.00");
        BigDecimal amount = totalAmount.subtract(taxAmount).subtract(serviceCharge).subtract(deliveryCharge);

        Map<String, String> params = new HashMap<>();
        params.put("amount", amount.toPlainString());
        params.put("tax_amount", "0");
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
