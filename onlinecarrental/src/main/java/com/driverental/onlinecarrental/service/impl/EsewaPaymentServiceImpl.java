package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.config.EsewaProperties;
import com.driverental.onlinecarrental.model.dto.request.EsewaInitiateRequest;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.model.exception.ResourceNotFoundException;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.PaymentRepository;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.EsewaPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
    public Map<String, String> initiate(EsewaInitiateRequest request) {
        Long bookingId = request.getBookingId();
        
        if (bookingId == null) {
            // No booking ID, just generate UUID for the payment
            String uuid = UUID.randomUUID().toString();
            BigDecimal amount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
            return buildEsewaFormParams(uuid, amount, BigDecimal.ZERO, amount);
        }
        
        // Use database-level locking with pessimistic write to prevent race conditions
        return createOrGetPaymentWithLock(bookingId);
    }

    /**
     * Creates or gets a payment for a booking using pessimistic write lock.
     * This prevents concurrent inserts for the same booking.
     */
    private Map<String, String> createOrGetPaymentWithLock(Long bookingId) {
        return transactionTemplate.execute(status -> {
            // Use pessimistic lock to prevent concurrent inserts
            Optional<Payment> existingPayment = paymentRepository.findByBookingIdWithLock(bookingId);
            if (existingPayment.isPresent()) {
                log.info("Existing payment found for booking {}. Using payment ID: {}", 
                        bookingId, existingPayment.get().getId());
                Payment payment = existingPayment.get();
                BigDecimal baseAmount = payment.getAmount();
                BigDecimal taxAmount = baseAmount.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalAmount = baseAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
                return buildEsewaFormParams(payment.getTransactionId(), baseAmount, taxAmount, totalAmount);
            }
            
            // Load booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
            
            // Create new payment
            BigDecimal baseAmount = booking.getTotalPrice();
            BigDecimal taxAmount = baseAmount.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalAmount = baseAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
            
            String uuid = UUID.randomUUID().toString();
            
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(totalAmount)
                    .paymentMethod(PaymentMethod.ESEWA)
                    .status(PaymentStatus.PROCESSING)
                    .transactionId(uuid)
                    .build();
            
            Payment saved = paymentRepository.save(payment);
            log.info("Payment created successfully. Payment ID: {}, Booking ID: {}, UUID: {}", 
                    saved.getId(), bookingId, uuid);
            
            return buildEsewaFormParams(uuid, baseAmount, taxAmount, totalAmount);
        });
    }

    @Override
    public Map<String, Object> verify(String transactionUuid, String totalAmount) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        
        // Get status URL from properties or use default
        String statusUrl = esewaProperties.getStatusUrl();
        if (statusUrl == null || statusUrl.isEmpty()) {
            statusUrl = esewaProperties.getDefaultStatusUrl();
        }

        String url = UriComponentsBuilder.fromHttpUrl(statusUrl)
                .queryParam("product_code", esewaProperties.getProductCode())
                .queryParam("transaction_uuid", transactionUuid)
                .queryParam("total_amount", totalAmount)
                .toUriString();

        log.info("Verifying payment with eSewa. URL: {}, Transaction UUID: {}", url, transactionUuid);

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = response.getBody() != null ? response.getBody() : new HashMap<>();

        Object statusObj = body.get("status");
        String status = statusObj != null ? statusObj.toString() : "UNKNOWN";
        
        // eSewa returns: "Complete" for successful payment
        boolean isSuccessful = "Complete".equalsIgnoreCase(status);

        // Update payment in a clean transaction
        final String finalUuid = transactionUuid;
        final boolean finalSuccess = isSuccessful;
        final String finalStatus = status;
        
        transactionTemplate.execute(txStatus -> {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(finalUuid);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (finalSuccess) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setCompletedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    log.info("Payment completed successfully. Transaction UUID: {}", finalUuid);
                    
                    // Confirm booking
                    if (payment.getBooking() != null) {
                        bookingService.confirmBooking(payment.getBooking().getId());
                    }
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    log.warn("Payment verification failed. Transaction UUID: {}, Status: {}", finalUuid, finalStatus);
                }
            } else {
                log.warn("No payment found for transaction UUID: {}", finalUuid);
            }
            return null;
        });

        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("transaction_uuid", transactionUuid);
        result.put("is_successful", isSuccessful);
        result.put("raw", body);
        return result;
    }

    /**
     * Builds eSewa form parameters for payment redirect
     * 
     * Required parameters according to eSewa documentation:
     * - amount: Base price (without tax)
     * - tax_amount: Tax amount (13% VAT)
     * - total_amount: Total amount (amount + tax_amount)
     * - transaction_uuid: Unique transaction identifier
     * - product_code: Merchant code
     * - product_service_charge: Service charge (optional)
     * - product_delivery_charge: Delivery charge (optional)
     * - success_url: Callback URL on success
     * - failure_url: Callback URL on failure
     * - signed_field_names: Comma-separated list of signed fields
     * - signature: HMAC-SHA256 signature
     */
    private Map<String, String> buildEsewaFormParams(String transactionUuid, BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal totalAmount) {
        // Ensure amounts are properly scaled
        baseAmount = baseAmount.setScale(2, RoundingMode.HALF_UP);
        taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        // Format amounts for eSewa (whole numbers without decimals)
        String totalAmountStr = totalAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(totalAmount.intValue())
            : totalAmount.toPlainString();

        String baseAmountStr = baseAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(baseAmount.intValue())
            : baseAmount.toPlainString();

        String taxAmountStr = taxAmount.stripTrailingZeros().scale() <= 0
            ? String.valueOf(taxAmount.intValue())
            : taxAmount.toPlainString();

        Map<String, String> params = new HashMap<>();
        
        // Required parameters
        params.put("amount", baseAmountStr);
        params.put("tax_amount", taxAmountStr);
        params.put("total_amount", totalAmountStr);
        params.put("transaction_uuid", transactionUuid);
        params.put("product_code", esewaProperties.getProductCode());
        
        // Optional parameters
        params.put("product_service_charge", "0");
        params.put("product_delivery_charge", "0");
        
        // Callback URLs
        params.put("success_url", esewaProperties.getSuccessUrl());
        params.put("failure_url", esewaProperties.getFailureUrl());

        // Signature
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        params.put("signed_field_names", signedFieldNames);

        // Generate signature
        String signatureMessage = String.format("total_amount=%s,transaction_uuid=%s,product_code=%s",
            totalAmountStr, transactionUuid, esewaProperties.getProductCode());
        params.put("signature", hmacSha256Base64(esewaProperties.getSecretKey(), signatureMessage));

        log.info("Built eSewa form params. Transaction UUID: {}, Total Amount: {}", transactionUuid, totalAmountStr);
        
        return params;
    }

    /**
     * Generates HMAC-SHA256 signature for eSewa payment request
     * 
     * @param secret The secret key provided by eSewa
     * @param message The message to sign (format: key1=value1,key2=value2,...)
     * @return Base64 encoded signature
     */
    private String hmacSha256Base64(String secret, String message) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signed = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            log.error("Failed to generate eSewa signature", e);
            throw new RuntimeException("Failed to sign eSewa request", e);
        }
    }
}
