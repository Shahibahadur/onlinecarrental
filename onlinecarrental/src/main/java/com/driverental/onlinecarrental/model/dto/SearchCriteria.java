package com.driverental.onlinecarrental.model.dto;

import com.driverental.onlinecarrental.model.enum.FuelType;
import com.driverental.onlinecarrental.model.enum.VehicleType;
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
public class SearchCriteria {
    private String query;
    private String location;
    private VehicleType vehicleType;
    private FuelType fuelType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> features;
    private String startDate;
    private String endDate;
    private Integer minSeats;
    private Integer maxSeats;
}