package com.driverental.onlinecarrental.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Slf4j
public class PriceCalculator {

    private static final Set<Month> PEAK_SEASON_MONTHS = Set.of(
        Month.JUNE, Month.JULY, Month.AUGUST, Month.DECEMBER
    );
    
    private static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    );

    // Price calculation methods
    
    /**
     * Calculate total price for a booking period
     */
    public static BigDecimal calculateTotalPrice(BigDecimal dailyRate, LocalDate startDate, LocalDate endDate) {
        long rentalDays = DateUtils.calculateDaysBetween(startDate, endDate);
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Invalid date range: start date must be before end date");
        }
        
        return dailyRate.multiply(BigDecimal.valueOf(rentalDays))
                       .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Apply seasonal pricing multiplier
     */
    public static BigDecimal applySeasonalPricing(BigDecimal price, LocalDate date) {
        BigDecimal multiplier = getSeasonalMultiplier(date);
        return price.multiply(multiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Apply weekend pricing multiplier
     */
    public static BigDecimal applyWeekendPricing(BigDecimal price, LocalDate date) {
        BigDecimal multiplier = getWeekendMultiplier(date);
        return price.multiply(multiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate discount for long-term rentals
     */
    public static BigDecimal applyLongTermDiscount(BigDecimal price, long rentalDays) {
        BigDecimal discountMultiplier = getLongTermDiscountMultiplier(rentalDays);
        return price.multiply(discountMultiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate lead time pricing adjustment
     */
    public static BigDecimal applyLeadTimePricing(BigDecimal price, LocalDate startDate) {
        BigDecimal multiplier = getLeadTimeMultiplier(startDate);
        return price.multiply(multiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate dynamic pricing based on multiple factors
     */
    public static BigDecimal calculateDynamicPrice(BigDecimal basePrice, LocalDate startDate, 
                                                  LocalDate endDate, long totalBookingsInPeriod) {
        long rentalDays = DateUtils.calculateDaysBetween(startDate, endDate);
        
        // Start with base calculation
        BigDecimal price = calculateTotalPrice(basePrice, startDate, endDate);
        
        // Apply seasonal pricing for each day in the rental period
        BigDecimal seasonalAdjustedPrice = applySeasonalAdjustment(price, startDate, endDate);
        
        // Apply weekend pricing
        BigDecimal weekendAdjustedPrice = applyWeekendAdjustment(seasonalAdjustedPrice, startDate, endDate);
        
        // Apply long-term discount
        BigDecimal discountedPrice = applyLongTermDiscount(weekendAdjustedPrice, rentalDays);
        
        // Apply lead time pricing
        BigDecimal leadTimeAdjustedPrice = applyLeadTimePricing(discountedPrice, startDate);
        
        // Apply demand-based pricing
        BigDecimal finalPrice = applyDemandPricing(leadTimeAdjustedPrice, totalBookingsInPeriod);
        
        log.debug("Price calculation - Base: {}, Seasonal: {}, Weekend: {}, Discounted: {}, LeadTime: {}, Final: {}",
                 price, seasonalAdjustedPrice, weekendAdjustedPrice, discountedPrice, leadTimeAdjustedPrice, finalPrice);
        
        return finalPrice;
    }

    /**
     * Calculate cancellation fee based on cancellation policy
     */
    public static BigDecimal calculateCancellationFee(BigDecimal totalPrice, LocalDate startDate, 
                                                     LocalDate cancellationDate) {
        long daysUntilStart = DateUtils.calculateDaysBetween(cancellationDate, startDate);
        
        BigDecimal cancellationFee = BigDecimal.ZERO;
        
        if (daysUntilStart < 1) {
            // Same day cancellation - 100% fee
            cancellationFee = totalPrice;
        } else if (daysUntilStart < 3) {
            // 1-2 days notice - 75% fee
            cancellationFee = totalPrice.multiply(BigDecimal.valueOf(0.75));
        } else if (daysUntilStart < 7) {
            // 3-6 days notice - 50% fee
            cancellationFee = totalPrice.multiply(BigDecimal.valueOf(0.50));
        } else if (daysUntilStart < 14) {
            // 7-13 days notice - 25% fee
            cancellationFee = totalPrice.multiply(BigDecimal.valueOf(0.25));
        }
        // 14+ days notice - no fee
        
        return cancellationFee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate refund amount after cancellation
     */
    public static BigDecimal calculateRefundAmount(BigDecimal totalPrice, BigDecimal cancellationFee) {
        return totalPrice.subtract(cancellationFee)
                        .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate insurance cost for booking
     */
    public static BigDecimal calculateInsuranceCost(BigDecimal totalPrice, String insuranceType) {
        BigDecimal insuranceRate = getInsuranceRate(insuranceType);
        return totalPrice.multiply(insuranceRate)
                        .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate additional fees (cleaning, delivery, etc.)
     */
    public static BigDecimal calculateAdditionalFees(String[] additionalServices) {
        BigDecimal totalFees = BigDecimal.ZERO;
        
        for (String service : additionalServices) {
            BigDecimal fee = getServiceFee(service);
            totalFees = totalFees.add(fee);
        }
        
        return totalFees.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate tax amount
     */
    public static BigDecimal calculateTax(BigDecimal subtotal, String stateCode) {
        BigDecimal taxRate = getTaxRate(stateCode);
        return subtotal.multiply(taxRate)
                      .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate final total with all fees and taxes
     */
    public static BigDecimal calculateFinalTotal(BigDecimal basePrice, BigDecimal insuranceCost,
                                                BigDecimal additionalFees, BigDecimal tax) {
        return basePrice.add(insuranceCost)
                       .add(additionalFees)
                       .add(tax)
                       .setScale(2, RoundingMode.HALF_UP);
    }

    // Helper methods for multipliers and rates
    
    private static BigDecimal getSeasonalMultiplier(LocalDate date) {
        if (PEAK_SEASON_MONTHS.contains(date.getMonth())) {
            return BigDecimal.valueOf(1.3); // 30% higher in peak season
        }
        return BigDecimal.ONE;
    }

    private static BigDecimal getWeekendMultiplier(LocalDate date) {
        if (WEEKEND_DAYS.contains(date.getDayOfWeek())) {
            return BigDecimal.valueOf(1.25); // 25% higher on weekends
        }
        return BigDecimal.ONE;
    }

    private static BigDecimal getLongTermDiscountMultiplier(long rentalDays) {
        if (rentalDays >= 30) {
            return BigDecimal.valueOf(0.80); // 20% discount for 30+ days
        } else if (rentalDays >= 14) {
            return BigDecimal.valueOf(0.85); // 15% discount for 14-29 days
        } else if (rentalDays >= 7) {
            return BigDecimal.valueOf(0.90); // 10% discount for 7-13 days
        } else if (rentalDays >= 3) {
            return BigDecimal.valueOf(0.95); // 5% discount for 3-6 days
        }
        return BigDecimal.ONE; // No discount for short rentals
    }

    private static BigDecimal getLeadTimeMultiplier(LocalDate startDate) {
        long daysUntilStart = DateUtils.calculateDaysBetween(LocalDate.now(), startDate);
        
        if (daysUntilStart <= 1) {
            return BigDecimal.valueOf(1.20); // 20% premium for last-minute
        } else if (daysUntilStart <= 3) {
            return BigDecimal.valueOf(1.10); // 10% premium for urgent
        } else if (daysUntilStart >= 60) {
            return BigDecimal.valueOf(0.90); // 10% discount for very early booking
        } else if (daysUntilStart >= 30) {
            return BigDecimal.valueOf(0.95); // 5% discount for early booking
        }
        return BigDecimal.ONE;
    }

    private static BigDecimal getInsuranceRate(String insuranceType) {
        return switch (insuranceType.toUpperCase()) {
            case "PREMIUM" -> BigDecimal.valueOf(0.15); // 15% of total
            case "STANDARD" -> BigDecimal.valueOf(0.10); // 10% of total
            case "BASIC" -> BigDecimal.valueOf(0.05);    // 5% of total
            default -> BigDecimal.ZERO;                  // No insurance
        };
    }

    private static BigDecimal getServiceFee(String service) {
        return switch (service.toUpperCase()) {
            case "DELIVERY" -> BigDecimal.valueOf(25.00);
            case "CLEANING" -> BigDecimal.valueOf(15.00);
            case "EXTRA_DRIVER" -> BigDecimal.valueOf(10.00);
            case "CHILD_SEAT" -> BigDecimal.valueOf(5.00);
            case "GPS" -> BigDecimal.valueOf(3.00);
            default -> BigDecimal.ZERO;
        };
    }

    private static BigDecimal getTaxRate(String stateCode) {
        // Simplified tax rates by state
        return switch (stateCode.toUpperCase()) {
            case "CA" -> BigDecimal.valueOf(0.0825); // 8.25% in California
            case "NY" -> BigDecimal.valueOf(0.08875); // 8.875% in New York
            case "TX" -> BigDecimal.valueOf(0.0825); // 8.25% in Texas
            case "FL" -> BigDecimal.valueOf(0.07);   // 7% in Florida
            default -> BigDecimal.valueOf(0.06);     // 6% default
        };
    }

    // Complex adjustment methods
    
    private static BigDecimal applySeasonalAdjustment(BigDecimal price, LocalDate startDate, LocalDate endDate) {
        BigDecimal adjustedPrice = BigDecimal.ZERO;
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            BigDecimal dailyPrice = price.divide(
                BigDecimal.valueOf(DateUtils.calculateDaysBetween(startDate, endDate)), 
                2, RoundingMode.HALF_UP
            );
            
            BigDecimal seasonalDailyPrice = applySeasonalPricing(dailyPrice, currentDate);
            adjustedPrice = adjustedPrice.add(seasonalDailyPrice);
            currentDate = currentDate.plusDays(1);
        }
        
        return adjustedPrice;
    }

    private static BigDecimal applyWeekendAdjustment(BigDecimal price, LocalDate startDate, LocalDate endDate) {
        BigDecimal adjustedPrice = BigDecimal.ZERO;
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            BigDecimal dailyPrice = price.divide(
                BigDecimal.valueOf(DateUtils.calculateDaysBetween(startDate, endDate)), 
                2, RoundingMode.HALF_UP
            );
            
            BigDecimal weekendDailyPrice = applyWeekendPricing(dailyPrice, currentDate);
            adjustedPrice = adjustedPrice.add(weekendDailyPrice);
            currentDate = currentDate.plusDays(1);
        }
        
        return adjustedPrice;
    }

    private static BigDecimal applyDemandPricing(BigDecimal price, long totalBookings) {
        BigDecimal demandMultiplier = getDemandMultiplier(totalBookings);
        return price.multiply(demandMultiplier)
                   .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal getDemandMultiplier(long totalBookings) {
        if (totalBookings > 1000) {
            return BigDecimal.valueOf(1.4); // High demand - 40% increase
        } else if (totalBookings > 500) {
            return BigDecimal.valueOf(1.2); // Medium demand - 20% increase
        } else if (totalBookings < 100) {
            return BigDecimal.valueOf(0.8); // Low demand - 20% discount
        }
        return BigDecimal.ONE;
    }

    /**
     * Validate if price is within acceptable range
     */
    public static boolean isValidPrice(BigDecimal price, BigDecimal minPrice, BigDecimal maxPrice) {
        return price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
    }

    /**
     * Format price for display
     */
    public static String formatPrice(BigDecimal price) {
        return "$" + price.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * Calculate percentage difference between two prices
     */
    public static BigDecimal calculatePercentageDifference(BigDecimal originalPrice, BigDecimal newPrice) {
        if (originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal difference = newPrice.subtract(originalPrice);
        return difference.divide(originalPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate price per day for comparison
     */
    public static BigDecimal calculatePricePerDay(BigDecimal totalPrice, long rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental days must be positive");
        }
        
        return totalPrice.divide(BigDecimal.valueOf(rentalDays), 2, RoundingMode.HALF_UP);
    }
}