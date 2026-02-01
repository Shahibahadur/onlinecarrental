package com.driverental.onlinecarrental.model.dto.request;

import com.driverental.onlinecarrental.model.enums.FuelType;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank
    @Size(max = 50)
    private String make;

    @NotBlank
    @Size(max = 50)
    private String model;

    @NotNull
    private Integer year;

    @NotNull
    private VehicleType type;

    @NotNull
    private FuelType fuelType;

    @NotBlank
    @Size(max = 100)
    private String transmission;

    @NotNull
    private Integer seats;

    @NotNull
    private Integer luggageCapacity;

    private List<String> features;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal dailyPrice;

    @NotBlank
    @Size(max = 100)
    private String location;

    private String imageName;

    private String registrationNumber;

    @NotNull
    private Boolean isAvailable;
}
