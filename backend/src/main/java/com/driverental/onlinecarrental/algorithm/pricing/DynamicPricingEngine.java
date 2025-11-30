package com.driverental.onlinecarrental.algorithm.pricing;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicPricingEngine {
    
    private final DemandCalculator demandCalculator;
    
    public BigDecimal calculateDynamicPrice(Vehicle vehicle, LocalDate startDate, 
                                          LocalDate endDate, long totalBookings) {
        BigDecimal basePrice = vehicle.getBasePrice();
        
        // Calculate various factors
        double demandFactor = demandCalculator.calculateDemandFactor(startDate, endDate, totalBookings);
        double durationFactor = calculateDurationFactor(startDate, endDate);
        double leadTimeFactor = calculateLeadTimeFactor(startDate);
        double vehicleFactor = calculateVehicleFactor(vehicle);
        
        // Apply pricing formula
        double finalMultiplier = demandFactor * durationFactor * leadTimeFactor * vehicleFactor;
        BigDecimal dynamicPrice = basePrice.multiply(BigDecimal.valueOf(finalMultiplier));
        
        // Ensure price doesn't go below base price
        if (dynamicPrice.compareTo(basePrice) < 0) {
            dynamicPrice = basePrice;
        }
        
        log.info("Dynamic pricing calculation: Base={}, Final={}, Multiplier={}", 
                basePrice, dynamicPrice, finalMultiplier);
        
        return dynamicPrice.setScale(2, RoundingMode.HALF_UP);
    }
    
    private double calculateDurationFactor(LocalDate startDate, LocalDate endDate) {
        long days = endDate.toEpochDay() - startDate.toEpochDay();
        
        if (days >= 14) {
            return 0.85; // 15% discount for 2+ weeks
        } else if (days >= 7) {
            return 0.90; // 10% discount for 1+ weeks
        } else if (days >= 3) {
            return 0.95; // 5% discount for 3+ days
        }
        
        return 1.0; // No discount for short rentals
    }
    
    private double calculateLeadTimeFactor(LocalDate startDate) {
        long daysUntilStart = startDate.toEpochDay() - LocalDate.now().toEpochDay();
        
        if (daysUntilStart <= 1) {
            return 1.20; // 20% premium for last-minute bookings
        } else if (daysUntilStart <= 3) {
            return 1.10; // 10% premium for urgent bookings
        } else if (daysUntilStart >= 30) {
            return 0.95; // 5% discount for early bookings
        } else if (daysUntilStart >= 14) {
            return 0.98; // 2% discount for advance bookings
        }
        
        return 1.0;
    }
    
    private double calculateVehicleFactor(Vehicle vehicle) {
        double factor = 1.0;
        
        // Adjust based on vehicle rating
        if (vehicle.getRating() >= 4.5) {
            factor *= 1.15; // 15% premium for highly rated vehicles
        } else if (vehicle.getRating() >= 4.0) {
            factor *= 1.08; // 8% premium for well-rated vehicles
        }
        
        // Adjust based on vehicle type
        switch (vehicle.getType()) {
            case LUXURY:
            case SPORTS:
                factor *= 1.25; // 25% premium for luxury/sports
                break;
            case SUV:
                factor *= 1.10; // 10% premium for SUVs
                break;
            case ELECTRIC:
                factor *= 1.15; // 15% premium for electric vehicles
                break;
        }
        
        return factor;
    }
}