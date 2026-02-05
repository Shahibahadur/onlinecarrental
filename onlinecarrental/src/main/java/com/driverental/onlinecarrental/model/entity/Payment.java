package com.driverental.onlinecarrental.model.entity;

import com.driverental.onlinecarrental.model.enums.PaymentStatus;
import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @NotNull
    @lombok.Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
