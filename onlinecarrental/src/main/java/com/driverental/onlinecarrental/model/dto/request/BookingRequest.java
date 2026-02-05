package com.driverental.onlinecarrental.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private String startDate;  // Date validation handled in BookingService

    @NotNull
    private String endDate;    // Date validation handled in BookingService

    @NotNull
    private String pickupLocation;

    @NotNull
    private String dropoffLocation;
}