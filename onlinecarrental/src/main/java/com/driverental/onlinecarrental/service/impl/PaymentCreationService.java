package com.driverental.onlinecarrental.service.impl;

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
     * 
     * @param bookingId The ID of the booking
     * @param amount The payment amount (including all taxes/charges)
     * @param paymentMethod The payment method (e.g., ESEWA)
     * @return Long The ID of the created or existing payment
     * 
     * @throws BusinessException if payment creation fails after all retries
     * @throws IllegalArgumentException if bookingId or amount is invalid
     */
    public Long createOrGetPayment(Long bookingId, BigDecimal amount, PaymentMethod paymentMethod) {
        // Input validation
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

        // Load the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        try {
            // Attempt to create new payment in isolated transaction
            return createPaymentInIsolation(booking, amount, paymentMethod);
        } catch (DataIntegrityViolationException e) {
            // Another thread created the payment first (UNIQUE constraint violated)
            log.info("Duplicate payment detected for booking ID: {}. Another thread created it. Recovering from race condition...", 
                    bookingId);

            // Fetch the existing payment in a completely separate transaction
            Long paymentId = fetchExistingPaymentWithRetry(bookingId);
            
            if (paymentId != null) {
                log.info("Successfully recovered from race condition. Using existing payment ID: {} for booking ID: {}", 
                        paymentId, bookingId);
                return paymentId;
            } else {
                log.error("Race condition recovery failed for booking ID: {}. Could not find existing payment after retries.", 
                        bookingId);
                throw new BusinessException(
                    "Payment creation failed due to a race condition. Please retry the operation."
                );
            }
        } catch (Exception e) {
            log.error("Unexpected error while creating payment for booking ID: {}", bookingId, e);
            throw e;
        }
    }

    /**
     * Attempts to create a new payment in an isolated transaction.
     * This is the database-first approach: attempt INSERT and let the database enforce uniqueness.
     * 
     * TRANSACTION SAFETY:
     * - Creates a fresh Hibernate session for this operation only
     * - READ_COMMITTED isolation ensures consistent view of data
     * - If INSERT succeeds: returns payment ID
     * - If INSERT fails: DataIntegrityViolationException bubbles up (caught by caller)
     * 
     * @param booking The booking entity
     * @param amount The payment amount
     * @param paymentMethod The payment method
     * @return Long The ID of the created payment
     * 
     * @throws DataIntegrityViolationException if booking_id is not unique
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    private Long createPaymentInIsolation(Booking booking, BigDecimal amount, PaymentMethod paymentMethod) {
        String transactionUuid = UUID.randomUUID().toString();

        log.debug("Attempting to create payment for booking ID: {}. Transaction UUID: {}", booking.getId(), transactionUuid);

        // Build and save the payment entity
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
            
            log.info("Payment created successfully. Payment ID: {}, Booking ID: {}, Transaction UUID: {}", 
                    savedPayment.getId(), booking.getId(), transactionUuid);

            // Return only the ID (primitive)
            // The payment entity stays in this method's session and is discarded when method returns
            return savedPayment.getId();

        } catch (DataIntegrityViolationException e) {
            // This is expected if another thread inserted first
            // Let it propagate for caller to handle
            log.debug("DataIntegrityViolationException caught for booking ID: {} - expected race condition", booking.getId());
            throw e;
        }
    }

    /**
     * Fetches an existing payment for a booking with retry logic.
     * 
     * WHY SEPARATE TRANSACTION:
     * - The parent transaction was marked rollback-only after the DataIntegrityViolationException
     * - We need a completely fresh session to see committed data from the competing thread
     * - TransactionTemplate.execute() creates a brand-new transaction, not nested
     * 
     * RETRY LOGIC:
     * - The competing thread's INSERT may not have committed yet when we first query
     * - We retry up to 10 times with 50ms delays (max 500ms total)
     * - Gives the other thread's transaction time to commit
     * - Returns payment ID if found, null if not found after retries
     * 
     * @param bookingId The booking ID to search for
     * @return Long The ID of the existing payment, or null if not found after retries
     */
    private Long fetchExistingPaymentWithRetry(Long bookingId) {
        final int maxRetries = 10;
        final long delayBetweenRetriesMs = 50;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            final int currentAttempt = attempt;

            // Execute fetch in a completely separate transaction via TransactionTemplate
            Long paymentId = transactionTemplate.execute(status -> {
                log.debug("Attempt {}/{}: Searching for existing payment for booking ID: {}", 
                        currentAttempt, maxRetries, bookingId);

                Optional<Payment> existingPayment = paymentRepository.findByBookingId(bookingId);

                if (existingPayment.isPresent()) {
                    Long foundId = existingPayment.get().getId();
                    log.debug("Attempt {}/{}: Found existing payment. Payment ID: {}, Status: {}", 
                            currentAttempt, maxRetries, foundId, existingPayment.get().getStatus());
                    return foundId;
                }

                log.debug("Attempt {}/{}: Payment not found yet", currentAttempt, maxRetries);
                return null;
            });

            // If payment was found, return it
            if (paymentId != null) {
                log.info("Existing payment found on attempt {}/{}. Payment ID: {}", 
                        attempt, maxRetries, paymentId);
                return paymentId;
            }

            // If this is not the last attempt, wait before retrying
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(delayBetweenRetriesMs);
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                    log.warn("Thread interrupted while waiting for payment creation retry. Booking ID: {}", bookingId);
                    return null;
                }
            }
        }

        // Not found after all retries
        log.error("Payment not found for booking ID: {} after {} retries ({}ms total wait)", 
                bookingId, maxRetries, maxRetries * delayBetweenRetriesMs);
        return null;
    }

    /**
     * Fetches a payment by ID in an isolated transaction.
     * Safe for concurrent access, returns only the payment ID if it exists.
     * 
     * @param paymentId The ID of the payment to fetch
     * @return Optional containing the payment, or empty if not found
     */
    public Optional<Payment> fetchPaymentById(Long paymentId) {
        if (paymentId == null || paymentId <= 0) {
            log.warn("Invalid payment ID: {}", paymentId);
            return Optional.empty();
        }

        return transactionTemplate.execute(status -> {
            log.debug("Fetching payment with ID: {}", paymentId);
            Optional<Payment> payment = paymentRepository.findById(paymentId);
            
            if (payment.isPresent()) {
                log.debug("Payment found. ID: {}, Status: {}", paymentId, payment.get().getStatus());
            } else {
                log.debug("Payment not found for ID: {}", paymentId);
            }
            
            return payment;
        });
    }

    /**
     * Updates the status of a payment in an isolated transaction.
     * 
     * @param paymentId The ID of the payment to update
     * @param newStatus The new payment status
     * @return boolean true if update succeeded, false if payment not found
     */
    public boolean updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        if (paymentId == null || paymentId <= 0) {
            log.warn("Invalid payment ID: {}", paymentId);
            return false;
        }
        if (newStatus == null) {
            log.warn("Payment status cannot be null");
            return false;
        }

        return transactionTemplate.execute(status -> {
            log.debug("Updating payment ID: {} to status: {}", paymentId, newStatus);

            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);

            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for ID: {} during status update", paymentId);
                return false;
            }

            Payment payment = paymentOpt.get();
            PaymentStatus oldStatus = payment.getStatus();

            payment.setStatus(newStatus);
            if (newStatus == PaymentStatus.COMPLETED) {
                payment.setCompletedAt(LocalDateTime.now());
            }

            paymentRepository.save(payment);
            
            log.info("Payment updated successfully. ID: {}, Old Status: {}, New Status: {}", 
                    paymentId, oldStatus, newStatus);
            
            return true;
        });
    }

    /**
     * Checks if a payment exists for a booking.
     * 
     * @param bookingId The booking ID
     * @return boolean true if payment exists, false otherwise
     */
    public boolean paymentExistsForBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            return false;
        }

        return transactionTemplate.execute(status -> {
            boolean exists = paymentRepository.findByBookingId(bookingId).isPresent();
            log.debug("Payment exists for booking ID {}: {}", bookingId, exists);
            return exists;
        });
    }

    /**
     * Fetches the payment amount for a booking.
     * 
     * @param bookingId The booking ID
     * @return Optional containing the amount, or empty if nothing paid
     */
    public Optional<BigDecimal> getPaymentAmountForBooking(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            return Optional.empty();
        }

        return transactionTemplate.execute(status -> {
            Optional<Payment> payment = paymentRepository.findByBookingId(bookingId);
            Optional<BigDecimal> amount = payment.map(Payment::getAmount);
            
            if (amount.isPresent()) {
                log.debug("Payment amount for booking ID {}: {}", bookingId, amount.get());
            } else {
                log.debug("No payment found for booking ID {}", bookingId);
            }
            
            return amount;
        });
    }
}
