package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.pricing.DynamicPricingEngine;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingServiceImpl implements PricingService {
    
    private final DynamicPricingEngine pricingEngine;
    private final BookingRepository bookingRepository;
    
    @Override
    public BigDecimal calculateBookingPrice(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        // Get total bookings for demand calculation
        long totalBookings = bookingRepository.countByStartDateBetween(
            startDate.minusMonths(1), startDate.plusMonths(1));
        
        BigDecimal dailyPrice = pricingEngine.calculateDynamicPrice(
            vehicle, startDate, endDate, totalBookings);
        
        long days = endDate.toEpochDay() - startDate.toEpochDay();
        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }
    
    @Override
    public BigDecimal getPriceEstimate(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        return calculateBookingPrice(vehicle, startDate, endDate);
    }
}