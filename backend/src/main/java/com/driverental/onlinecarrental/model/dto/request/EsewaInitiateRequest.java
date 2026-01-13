package com.driverental.onlinecarrental.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsewaInitiateRequest {

    // For booking-based flow, prefer bookingId.
    // If bookingId is null, amount will be used.
    private Long bookingId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;
}
