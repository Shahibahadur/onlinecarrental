package com.driverental.onlinecarrental.model.dto.response;

import com.driverental.onlinecarrental.model.enums.FuelType;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private VehicleType type;
    private FuelType fuelType;
    private String transmission;
    private Integer seats;
    private Integer luggageCapacity;
    private List<String> features;
    private BigDecimal basePrice;
    private BigDecimal dailyPrice;
    private String location;
    private String imageUrl;
    private String description;
    private String licensePlate;
    private Double engineCapacity;
    private Boolean isAvailable;
    private Double rating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
}
