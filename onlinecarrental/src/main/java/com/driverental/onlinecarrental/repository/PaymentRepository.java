package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Payment;
import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByTransactionId(String transactionId);

    Long countByStatus(PaymentStatus status);
}
