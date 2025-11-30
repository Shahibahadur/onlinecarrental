package com.driverental.onlinecarrental.algorithm.pricing;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Component
public class DemandCalculator {
    
    private static final Set<Month> PEAK_MONTHS = Set.of(
        Month.JUNE, Month.JULY, Month.AUGUST, Month.DECEMBER
    );
    
    private static final Set<DayOfWeek> PEAK_DAYS = Set.of(
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    );
    
    public double calculateDemandFactor(LocalDate startDate, LocalDate endDate, long totalBookings) {
        double baseDemand = 1.0;
        
        // Seasonal demand
        baseDemand *= calculateSeasonalFactor(startDate);
        
        // Weekend vs weekday demand
        baseDemand *= calculateDayOfWeekFactor(startDate);
        
        // Historical booking density
        baseDemand *= calculateBookingDensityFactor(totalBookings);
        
        // Holiday demand (simplified)
        baseDemand *= calculateHolidayFactor(startDate);
        
        return Math.min(2.0, Math.max(0.5, baseDemand)); // Cap between 0.5x and 2.0x
    }
    
    private double calculateSeasonalFactor(LocalDate date) {
        if (PEAK_MONTHS.contains(date.getMonth())) {
            return 1.3; // 30% higher in peak months
        }
        return 1.0;
    }
    
    private double calculateDayOfWeekFactor(LocalDate date) {
        if (PEAK_DAYS.contains(date.getDayOfWeek())) {
            return 1.25; // 25% higher on weekends
        }
        return 1.0;
    }
    
    private double calculateBookingDensityFactor(long totalBookings) {
        if (totalBookings > 1000) {
            return 1.4; // High demand
        } else if (totalBookings > 500) {
            return 1.2; // Medium demand
        } else if (totalBookings < 100) {
            return 0.8; // Low demand
        }
        return 1.0;
    }
    
    private double calculateHolidayFactor(LocalDate date) {
        // Simplified holiday detection
        if ((date.getMonth() == Month.DECEMBER && date.getDayOfMonth() >= 20) ||
            (date.getMonth() == Month.JANUARY && date.getDayOfMonth() <= 5)) {
            return 1.5; // 50% higher during Christmas/New Year
        }
        return 1.0;
    }
}