package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingService {
    BigDecimal calculateBookingPrice(Vehicle vehicle, LocalDate startDate, LocalDate endDate);
}
