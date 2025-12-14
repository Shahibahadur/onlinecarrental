package com.driverental.onlinecarrental.service;

public interface PricingService {
    java.math.BigDecimal calculateBookingPrice(com.driverental.onlinecarrental.model.entity.Car car,
            java.time.LocalDate startDate, java.time.LocalDate endDate);
}
