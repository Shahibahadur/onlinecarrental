package com.driverental.onlinecarrental.service.internal;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTxHelper {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public Optional<Payment> findPaymentByBookingIdNewTx(Long bookingId) {
		return paymentRepository.findByBookingId(bookingId);
	}

	/**
	 * Attempt to create a payment for the given booking in an independent transaction.
	 * If a concurrent request wins the race, recover and return the winner's UUID.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String createOrGetPaymentUuidNewTx(Long bookingId, BigDecimal amount) {
		// Double-check if a payment already exists
		Optional<Payment> existing = paymentRepository.findByBookingId(bookingId);
		if (existing.isPresent()) {
			Payment payment = existing.get();
			if (payment.getTransactionId() != null) {
				return payment.getTransactionId();
			}
			String uuid = UUID.randomUUID().toString();
			payment.setTransactionId(uuid);
			paymentRepository.save(payment);
			return uuid;
		}

		// Try to create the payment
		try {
			// Acquire a pessimistic lock on the booking to serialize payment creation per booking
			Booking booking = entityManager.find(Booking.class, bookingId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
			if (booking == null) {
				booking = bookingRepository.findById(bookingId).orElseThrow();
			}

			// Re-check after locking to avoid TOCTOU
			Optional<Payment> afterLockExisting = paymentRepository.findByBookingId(bookingId);
			if (afterLockExisting.isPresent()) {
				Payment payment = afterLockExisting.get();
				return payment.getTransactionId() != null ? payment.getTransactionId() : UUID.randomUUID().toString();
			}

			String uuid = UUID.randomUUID().toString();
			Payment payment = Payment.builder()
					.booking(booking)
					.amount(amount)
					.paymentMethod(PaymentMethod.ESEWA)
					.status(PaymentStatus.PROCESSING)
					.transactionId(uuid)
					.build();
			Payment saved = paymentRepository.save(payment);
			log.info("Payment created successfully (new tx). ID: {}, UUID: {}", saved.getId(), uuid);
			return uuid;
		} catch (RuntimeException ex) {
			// Handle duplicate key across different exception wrappers
			boolean likelyDuplicate =
					ex instanceof DataIntegrityViolationException ||
					ex instanceof JpaSystemException ||
					(ex.getCause() instanceof ConstraintViolationException) ||
					(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("duplicate entry"));

			if (!likelyDuplicate) {
				throw ex;
			}

			log.warn("Duplicate payment detected in new tx for booking {}. Recovering winner. Cause: {}", bookingId, ex.getMessage());

			// Clear persistence context to avoid Hibernate assertion failures after exception
			try {
				entityManager.clear();
			} catch (Exception ignored) {
				// best effort
			}

			// Small bounded retry window
			long sleepMs = 50L;
			for (int i = 0; i < 40; i++) {
				Optional<Payment> winner = paymentRepository.findByBookingId(bookingId);
				if (winner.isPresent()) {
					Payment payment = winner.get();
					return payment.getTransactionId() != null ? payment.getTransactionId() : UUID.randomUUID().toString();
				}
				try {
					Thread.sleep(sleepMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
				sleepMs = Math.min(sleepMs + 25L, 200L);
			}

			// Final attempt
			Optional<Payment> winner = paymentRepository.findByBookingId(bookingId);
			if (winner.isPresent()) {
				Payment payment = winner.get();
				return payment.getTransactionId() != null ? payment.getTransactionId() : UUID.randomUUID().toString();
			}

			throw ex;
		}
	}
}

