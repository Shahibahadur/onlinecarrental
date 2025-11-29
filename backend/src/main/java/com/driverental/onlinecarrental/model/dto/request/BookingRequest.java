package com.driverental.onlinecarrental.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    @NotNull
    private Long vehicleId;

    @NotNull
    @Future
    private String startDate;

    @NotNull
    @Future
    private String endDate;

    @NotNull
    private String pickupLocation;

    @NotNull
    private String dropoffLocation;
}