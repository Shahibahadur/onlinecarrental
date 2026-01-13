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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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

        // Create/update local payment row (so we can link verification to a booking later)
        if (booking != null) {
            Payment payment = paymentRepository.findByBookingId(booking.getId())
                    .orElse(Payment.builder().booking(booking).build());
            payment.setAmount(amount);
            payment.setPaymentMethod(PaymentMethod.ESEWA);
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setTransactionId(uuid);
            paymentRepository.save(payment);
        }

        Map<String, String> params = buildEsewaFormParams(uuid, amount);
        return params;
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
        String totalAmountStr = totalAmount.stripTrailingZeros().toPlainString();

        Map<String, String> params = new HashMap<>();
        params.put("amount", totalAmountStr);
        params.put("tax_amount", "0");
        params.put("total_amount", totalAmountStr);
        params.put("transaction_uuid", transactionUuid);
        params.put("product_code", esewaProperties.getProductCode());
        params.put("product_service_charge", "0");
        params.put("product_delivery_charge", "0");
        params.put("success_url", esewaProperties.getSuccessUrl());
        params.put("failure_url", esewaProperties.getFailureUrl());

        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        params.put("signed_field_names", signedFieldNames);

        String signatureMessage = "total_amount=" + totalAmountStr + ",transaction_uuid=" + transactionUuid + ",product_code="
                + esewaProperties.getProductCode();
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
