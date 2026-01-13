package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.pricing.DynamicPricingEngine;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.service.PricingService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final DynamicPricingEngine dynamicPricingEngine;
    private final BookingRepository bookingRepository;

    @Override
    public BigDecimal calculateBookingPrice(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) {
            days = 1;
        }

        Long totalBookings = bookingRepository.countByStartDateBetween(startDate, endDate);
        if (totalBookings == null) {
            totalBookings = 0L;
        }

        // Calculate a dynamic daily rate (engine ensures it never goes below base price)
        BigDecimal dynamicDailyPrice = dynamicPricingEngine.calculateDynamicPrice(vehicle, startDate, endDate,
                totalBookings);

        return dynamicDailyPrice.multiply(BigDecimal.valueOf(days));
    }
}