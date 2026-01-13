package com.driverental.onlinecarrental.model.dto.response;

import com.driverental.onlinecarrental.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long vehicleId;
    private String startDate;
    private String endDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private VehicleResponse vehicle; // Optional: include vehicle details
    private UserResponse user; // Optional: include user details
}