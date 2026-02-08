package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.config.KhaltiConfig;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiCallbackDTO;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiRequest;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiResponse;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.model.exception.ResourceNotFoundException;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.PaymentRepository;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.KhaltiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhaltiServiceImpl implements KhaltiService {

    private final KhaltiConfig config;
    private final RestTemplate restTemplate;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Override
    @Transactional
    public KhaltiResponse initiatePayment(KhaltiRequest khalti) {
        // Extract booking ID from purchase order ID
        String bookingIdStr = khalti.getPurchase_order_id();
        Long bookingId = Long.parseLong(bookingIdStr);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Request body as per Khalti API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("return_url", config.getCallbackUrl());
        requestBody.put("website_url", config.getWebsiteUrl());
        requestBody.put("amount", khalti.getAmount()); // amount in paisa
        requestBody.put("purchase_order_id", khalti.getPurchase_order_id());
        requestBody.put("purchase_order_name", khalti.getPurchase_order_name());

        // Headers with authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + config.getLiveSecretKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<KhaltiResponse> response = restTemplate.exchange(
                    config.getInitialUrl(),
                    HttpMethod.POST,
                    entity,
                    KhaltiResponse.class
            );

            KhaltiResponse khaltiResponse = response.getBody();
            
            // Create/update payment record
                Payment payment = paymentRepository.findByBookingId(booking.getId())
                    .orElse(Payment.builder().booking(booking).build());
                BigDecimal base = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
                BigDecimal tax = base.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalWithTax = base.add(tax).setScale(2, RoundingMode.HALF_UP);
                payment.setAmount(totalWithTax);
            payment.setPaymentMethod(com.driverental.onlinecarrental.model.enums.PaymentMethod.KHALTI);
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setTransactionId(khaltiResponse.getPidx());
            paymentRepository.save(payment);

            log.info("Khalti payment initiated for booking: {}", bookingId);
            return khaltiResponse;
        } catch (Exception e) {
            log.error("Error initiating Khalti payment: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate Khalti payment", e);
        }
    }

    @Override
    @Transactional
    public boolean verifyPayment(KhaltiCallbackDTO response) {
        Payment payment = paymentRepository.findByTransactionId(response.getPidx()).orElse(null);
        
        if (!"COMPLETED".equalsIgnoreCase(response.getStatus())) {
            log.warn("Payment not completed for pidx: {}", response.getPidx());
            if (payment != null) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            return false;
        }

        // Verify payment with Khalti API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + config.getLiveSecretKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pidx", response.getPidx());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> verificationResponse = restTemplate.postForEntity(
                    config.getVerifyUrl(),
                    entity,
                    Map.class
            );

            if (verificationResponse.getStatusCode() == HttpStatus.OK && verificationResponse.getBody() != null) {
                Map<String, Object> body = verificationResponse.getBody();
                String status = (String) body.get("status");
                String transactionId = (String) body.get("transaction_id");

                if (transactionId != null && transactionId.equals(response.getTransaction_id()) 
                    && "Completed".equalsIgnoreCase(status)) {
                    
                    // Update payment status
                    if (payment != null) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setCompletedAt(LocalDateTime.now());
                        paymentRepository.save(payment);

                        // Confirm booking
                        if (payment.getBooking() != null) {
                            bookingService.confirmBooking(payment.getBooking().getId());
                        }
                    }
                    return true;
                } else {
                    if (payment != null) {
                        payment.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error verifying Khalti payment: {}", e.getMessage());
        }
        
        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
        return false;
    }

    @Override
    public String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
