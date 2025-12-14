package com.driverental.onlinecarrental.model.dto.response;

import com.driverental.onlinecarrental.model.enums.FuelType;
import com.driverental.onlinecarrental.model.enums.CarCategory;
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
public class CarResponse {
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private CarCategory type;
    private FuelType fuelType;
    private String transmission;
    private Integer seats;
    private Integer luggageCapacity;
    private List<String> features;
    private BigDecimal basePrice;
    private BigDecimal dailyPrice;
    private String location;
    private String imageUrl;
    private Boolean isAvailable;
    private Double rating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
}