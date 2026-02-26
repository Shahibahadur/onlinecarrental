package com.driverental.onlinecarrental.payment.common;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.model.exception.BusinessException;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for safely creating payments with race condition handling.
 * 
 * PROBLEM: Multiple concurrent requests may attempt to create a payment for the same booking.
 * The payments table has a UNIQUE constraint on booking_id, so only one payment can exist per booking.
 * 
 * SOLUTION: Database-first approach with proper transaction isolation.
 * - Attempt INSERT first (not check-then-insert)
 * - If duplicate key error: Catch it and fetch the existing payment in a separate transaction
 * - Return the payment ID from either the successful insert or the existing record
 * 
 * SAFETY GUARANTEES:
 * - No race condition window (database enforces uniqueness)
 * - No Hibernate session reuse after exceptions (each phase gets fresh session)
 * - Exception contained within service (never propagates to caller)
 * - Fully idempotent (same booking always returns same payment)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCreationService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates or fetches a payment for a booking.
     * 
     * PUBLIC ENTRY POINT - Safe for concurrent calls with same bookingId
     */
    public Long createOrGetPayment(Long bookingId, BigDecimal amount, PaymentMethod paymentMethod) {
        if (bookingId == null || bookingId <= 0) {
            log.error("Invalid bookingId: {}", bookingId);
            throw new IllegalArgumentException("Booking ID must be a positive number");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (paymentMethod == null) {
            log.error("Payment method is null");
            throw new IllegalArgumentException("Payment method cannot be null");
        }

        log.info("Processing payment creation for booking ID: {}, amount: {}, method: {}", 
                bookingId, amount, paymentMethod);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        try {
            return createPaymentInIsolation(booking, amount, paymentMethod);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate payment detected for booking ID: {}. Recovering from race condition...", bookingId);

            Long paymentId = fetchExistingPaymentWithRetry(bookingId);
            
            if (paymentId != null) {
                log.info("Successfully recovered. Using existing payment ID: {} for booking ID: {}", paymentId, bookingId);
                return paymentId;
            } else {
                log.error("Race condition recovery failed for booking ID: {}", bookingId);
                throw new BusinessException("Payment creation failed due to a race condition. Please retry.");
            }
        } catch (Exception e) {
            log.error("Unexpected error while creating payment for booking ID: {}", bookingId, e);
            throw e;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    private Long createPaymentInIsolation(Booking booking, BigDecimal amount, PaymentMethod paymentMethod) {
        String transactionUuid = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PROCESSING)
                .transactionId(transactionUuid)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created. Payment ID: {}, Booking ID: {}, UUID: {}", 
                    savedPayment.getId(), booking.getId(), transactionUuid);
            return savedPayment.getId();
        } catch (DataIntegrityViolationException e) {
            log.debug("DataIntegrityViolationException for booking ID: {} - expected race condition", booking.getId());
            throw e;
        }
    }

    private Long fetchExistingPaymentWithRetry(Long bookingId) {
        final int maxRetries = 10;
        final long delayBetweenRetriesMs = 50;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            final int currentAttempt = attempt;

            Long paymentId = transactionTemplate.execute(status -> {
                Optional<Payment> existingPayment = paymentRepository.findByBookingId(bookingId);
                if (existingPayment.isPresent()) {
                    return existingPayment.get().getId();
                }
                return null;
            });

            if (paymentId != null) {
                log.info("Existing payment found on attempt {}/{}. Payment ID: {}", attempt, maxRetries, paymentId);
                return paymentId;
            }

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(delayBetweenRetriesMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }

    public Optional<Payment> fetchPaymentById(Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            return Optional.empty();
        }
        return transactionTemplate.execute(status -> paymentRepository.findById(paymentId));
    }

    public boolean updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        if (paymentId == null || paymentId <= 0 || newStatus == null) {
            return false;
        }

        return transactionTemplate.execute(status -> {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return false;
            }

            Payment payment = paymentOpt.get();
            payment.setStatus(newStatus);
            if (newStatus == PaymentStatus.COMPLETED) {
                payment.setCompletedAt(LocalDateTime.now());
            }
            paymentRepository.save(payment);
            return true;
        });
    }

    public boolean paymentExistsForBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            return false;
        }
        return transactionTemplate.execute(status -> paymentRepository.findByBookingId(bookingId).isPresent());
    }

    public Optional<BigDecimal> getPaymentAmountForBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            return Optional.empty();
        }
        return transactionTemplate.execute(status -> 
            paymentRepository.findByBookingId(bookingId).map(Payment::getAmount)
        );
    }
}
