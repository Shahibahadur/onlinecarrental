package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.service.PricingService;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class PricingServiceImpl implements PricingService {

    @Override
    public BigDecimal calculateBookingPrice(Car car, LocalDate startDate, LocalDate endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateBookingPrice'");
    }
}