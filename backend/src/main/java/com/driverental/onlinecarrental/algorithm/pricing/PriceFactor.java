package com.driverental.onlinecarrental.algorithm.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a single factor that influences vehicle pricing
 * Each factor has a type, value, and weight in the overall pricing calculation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceFactor implements Comparable<PriceFactor> {
    
    /**
     * Type of pricing factor
     */
    private PriceFactorType type;
    
    /**
     * Descriptive name of the factor
     */
    private String name;
    
    /**
     * Multiplier value (e.g., 1.2 for 20% increase, 0.8 for 20% decrease)
     */
    private BigDecimal multiplier;
    
    /**
     * Weight of this factor in the overall calculation (0.0 to 1.0)
     */
    private BigDecimal weight;
    
    /**
     * Description of why this factor was applied
     */
    private String description;
    
    /**
     * Confidence level in this factor's accuracy (0.0 to 1.0)
     */
    @Builder.Default
    private BigDecimal confidence = BigDecimal.ONE;
    
    /**
     * Whether this factor increases price (multiplier > 1.0)
     */
    public boolean isPriceIncrease() {
        return multiplier.compareTo(BigDecimal.ONE) > 0;
    }
    
    /**
     * Whether this factor decreases price (multiplier < 1.0)
     */
    public boolean isPriceDecrease() {
        return multiplier.compareTo(BigDecimal.ONE) < 0;
    }
    
    /**
     * Whether this factor is neutral (multiplier = 1.0)
     */
    public boolean isNeutral() {
        return multiplier.compareTo(BigDecimal.ONE) == 0;
    }
    
    /**
     * Calculate the percentage change represented by this factor
     */
    public BigDecimal getPercentageChange() {
        return multiplier.subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get the impact of this factor on a base price
     */
    public BigDecimal calculateImpact(BigDecimal basePrice) {
        return basePrice.multiply(multiplier.subtract(BigDecimal.ONE))
                .multiply(weight)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Compare factors by weight for sorting
     */
    @Override
    public int compareTo(PriceFactor other) {
        return other.weight.compareTo(this.weight); // Descending order
    }
    
    /**
     * Create a string representation for display
     */
    @Override
    public String toString() {
        String changeType = isPriceIncrease() ? "increase" : 
                           isPriceDecrease() ? "decrease" : "neutral";
        return String.format("%s: %s (%.1f%%) [weight: %.2f, confidence: %.2f]", 
                name, changeType, getPercentageChange().doubleValue(), 
                weight.doubleValue(), confidence.doubleValue());
    }
    
    /**
     * Factory methods for common price factors
     */
    
    public static PriceFactor createSeasonalFactor(Month month, BigDecimal weight) {
        BigDecimal multiplier = getSeasonalMultiplier(month);
        String description = String.format("Seasonal pricing for %s", month);
        
        return PriceFactor.builder()
                .type(PriceFactorType.SEASONAL)
                .name("Seasonal Demand")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.9))
                .build();
    }
    
    public static PriceFactor createWeekendFactor(LocalDate date, BigDecimal weight) {
        BigDecimal multiplier = getWeekendMultiplier(date);
        String dayType = isWeekend(date) ? "weekend" : "weekday";
        String description = String.format("%s pricing for %s", dayType, date.getDayOfWeek());
        
        return PriceFactor.builder()
                .type(PriceFactorType.TIME_BASED)
                .name(dayType.equals("weekend") ? "Weekend Premium" : "Weekday Discount")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.8))
                .build();
    }
    
    public static PriceFactor createDemandFactor(long currentBookings, long capacity, BigDecimal weight) {
        BigDecimal multiplier = calculateDemandMultiplier(currentBookings, capacity);
        double utilization = (double) currentBookings / capacity * 100;
        String description = String.format("Demand-based pricing (%.1f%% utilization)", utilization);
        
        return PriceFactor.builder()
                .type(PriceFactorType.DEMAND)
                .name("Demand Level")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.7))
                .build();
    }
    
    public static PriceFactor createDurationFactor(long rentalDays, BigDecimal weight) {
        BigDecimal multiplier = calculateDurationMultiplier(rentalDays);
        String description = String.format("Duration discount for %d days", rentalDays);
        
        return PriceFactor.builder()
                .type(PriceFactorType.DURATION)
                .name("Rental Duration")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.95))
                .build();
    }
    
    public static PriceFactor createLeadTimeFactor(LocalDate startDate, BigDecimal weight) {
        BigDecimal multiplier = calculateLeadTimeMultiplier(startDate);
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        String description = String.format("Lead time pricing (%d days in advance)", daysUntilStart);
        
        return PriceFactor.builder()
                .type(PriceFactorType.LEAD_TIME)
                .name("Booking Lead Time")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.85))
                .build();
    }
    
    public static PriceFactor createVehicleRatingFactor(double rating, BigDecimal weight) {
        BigDecimal multiplier = calculateRatingMultiplier(rating);
        String description = String.format("Vehicle rating premium (%.1f stars)", rating);
        
        return PriceFactor.builder()
                .type(PriceFactorType.VEHICLE_QUALITY)
                .name("Vehicle Rating")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.9))
                .build();
    }
    
    public static PriceFactor createVehicleTypeFactor(String vehicleType, BigDecimal weight) {
        BigDecimal multiplier = calculateVehicleTypeMultiplier(vehicleType);
        String description = String.format("Vehicle type pricing for %s", vehicleType);
        
        return PriceFactor.builder()
                .type(PriceFactorType.VEHICLE_TYPE)
                .name("Vehicle Category")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.8))
                .build();
    }
    
    public static PriceFactor createLocationFactor(String location, BigDecimal weight) {
        BigDecimal multiplier = calculateLocationMultiplier(location);
        String description = String.format("Location-based pricing for %s", location);
        
        return PriceFactor.builder()
                .type(PriceFactorType.LOCATION)
                .name("Location Demand")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.6))
                .build();
    }
    
    public static PriceFactor createHolidayFactor(LocalDate date, BigDecimal weight) {
        BigDecimal multiplier = calculateHolidayMultiplier(date);
        String description = "Holiday season premium pricing";
        
        return PriceFactor.builder()
                .type(PriceFactorType.HOLIDAY)
                .name("Holiday Season")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.7))
                .build();
    }
    
    public static PriceFactor createCompetitionFactor(double competitorPrice, double ourPrice, BigDecimal weight) {
        BigDecimal multiplier = calculateCompetitionMultiplier(competitorPrice, ourPrice);
        double priceDifference = ((competitorPrice - ourPrice) / ourPrice) * 100;
        String description = String.format("Competitive pricing adjustment (%.1f%% difference)", priceDifference);
        
        return PriceFactor.builder()
                .type(PriceFactorType.COMPETITION)
                .name("Market Competition")
                .multiplier(multiplier)
                .weight(weight)
                .description(description)
                .confidence(BigDecimal.valueOf(0.5))
                .build();
    }
    
    // Helper methods for multiplier calculations
    
    private static BigDecimal getSeasonalMultiplier(Month month) {
        // Peak season months get higher prices
        Set<Month> peakMonths = EnumSet.of(Month.JUNE, Month.JULY, Month.AUGUST, Month.DECEMBER);
        Set<Month> shoulderMonths = EnumSet.of(Month.MAY, Month.SEPTEMBER, Month.OCTOBER);
        
        if (peakMonths.contains(month)) {
            return BigDecimal.valueOf(1.3); // 30% higher in peak season
        } else if (shoulderMonths.contains(month)) {
            return BigDecimal.valueOf(1.1); // 10% higher in shoulder season
        } else {
            return BigDecimal.valueOf(0.9); // 10% lower in off-season
        }
    }
    
    private static BigDecimal getWeekendMultiplier(LocalDate date) {
        return isWeekend(date) ? BigDecimal.valueOf(1.25) : BigDecimal.valueOf(0.95);
    }
    
    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
    
    private static BigDecimal calculateDemandMultiplier(long currentBookings, long capacity) {
        if (capacity == 0) return BigDecimal.ONE;
        
        double utilization = (double) currentBookings / capacity;
        
        if (utilization >= 0.9) {
            return BigDecimal.valueOf(1.5); // High demand - 50% increase
        } else if (utilization >= 0.7) {
            return BigDecimal.valueOf(1.2); // Medium demand - 20% increase
        } else if (utilization <= 0.3) {
            return BigDecimal.valueOf(0.8); // Low demand - 20% decrease
        } else {
            return BigDecimal.ONE; // Normal demand
        }
    }
    
    private static BigDecimal calculateDurationMultiplier(long rentalDays) {
        if (rentalDays >= 30) {
            return BigDecimal.valueOf(0.75); // 25% discount for 30+ days
        } else if (rentalDays >= 14) {
            return BigDecimal.valueOf(0.85); // 15% discount for 14-29 days
        } else if (rentalDays >= 7) {
            return BigDecimal.valueOf(0.90); // 10% discount for 7-13 days
        } else if (rentalDays >= 3) {
            return BigDecimal.valueOf(0.95); // 5% discount for 3-6 days
        } else {
            return BigDecimal.ONE; // No discount for short rentals
        }
    }
    
    private static BigDecimal calculateLeadTimeMultiplier(LocalDate startDate) {
        long daysUntilStart = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        
        if (daysUntilStart <= 1) {
            return BigDecimal.valueOf(1.3); // 30% premium for last-minute
        } else if (daysUntilStart <= 3) {
            return BigDecimal.valueOf(1.15); // 15% premium for urgent
        } else if (daysUntilStart >= 60) {
            return BigDecimal.valueOf(0.85); // 15% discount for very early booking
        } else if (daysUntilStart >= 30) {
            return BigDecimal.valueOf(0.90); // 10% discount for early booking
        } else if (daysUntilStart >= 14) {
            return BigDecimal.valueOf(0.95); // 5% discount for advance booking
        } else {
            return BigDecimal.ONE; // Standard pricing
        }
    }
    
    private static BigDecimal calculateRatingMultiplier(double rating) {
        if (rating >= 4.8) {
            return BigDecimal.valueOf(1.25); // 25% premium for excellent rating
        } else if (rating >= 4.5) {
            return BigDecimal.valueOf(1.15); // 15% premium for very good rating
        } else if (rating >= 4.0) {
            return BigDecimal.valueOf(1.05); // 5% premium for good rating
        } else if (rating <= 3.0) {
            return BigDecimal.valueOf(0.85); // 15% discount for poor rating
        } else {
            return BigDecimal.ONE; // Standard pricing
        }
    }
    
    private static BigDecimal calculateVehicleTypeMultiplier(String vehicleType) {
        return switch (vehicleType.toUpperCase()) {
            case "LUXURY", "SPORTS" -> BigDecimal.valueOf(1.4); // 40% premium
            case "SUV", "ELECTRIC" -> BigDecimal.valueOf(1.2);  // 20% premium
            case "SEDAN" -> BigDecimal.valueOf(1.0);            // Standard pricing
            case "HATCHBACK", "COMPACT" -> BigDecimal.valueOf(0.9); // 10% discount
            default -> BigDecimal.ONE;
        };
    }
    
    private static BigDecimal calculateLocationMultiplier(String location) {
        // Premium locations get higher prices
        Set<String> premiumLocations = Set.of("NEW YORK", "LOS ANGELES", "SAN FRANCISCO", "MIAMI");
        Set<String> discountLocations = Set.of("RURAL", "SUBURBAN");
        
        String upperLocation = location.toUpperCase();
        
        if (premiumLocations.stream().anyMatch(upperLocation::contains)) {
            return BigDecimal.valueOf(1.2); // 20% premium
        } else if (discountLocations.stream().anyMatch(upperLocation::contains)) {
            return BigDecimal.valueOf(0.9); // 10% discount
        } else {
            return BigDecimal.ONE; // Standard pricing
        }
    }
    
    private static BigDecimal calculateHolidayMultiplier(LocalDate date) {
        // Major holidays get premium pricing
        Set<LocalDate> majorHolidays = Set.of(
            LocalDate.of(date.getYear(), 12, 24), // Christmas Eve
            LocalDate.of(date.getYear(), 12, 25), // Christmas
            LocalDate.of(date.getYear(), 12, 31), // New Year's Eve
            LocalDate.of(date.getYear(), 1, 1),   // New Year's Day
            LocalDate.of(date.getYear(), 7, 4)    // Independence Day
        );
        
        // Check if date is within holiday period
        for (LocalDate holiday : majorHolidays) {
            long daysBetween = Math.abs(ChronoUnit.DAYS.between(date, holiday));
            if (daysBetween <= 3) { // 3 days before/after holiday
                return BigDecimal.valueOf(1.4); // 40% premium during holidays
            }
        }
        
        return BigDecimal.ONE; // Standard pricing
    }
    
    private static BigDecimal calculateCompetitionMultiplier(double competitorPrice, double ourPrice) {
        if (competitorPrice <= 0 || ourPrice <= 0) {
            return BigDecimal.ONE;
        }
        
        double ratio = competitorPrice / ourPrice;
        
        if (ratio < 0.8) {
            // Competitor is significantly cheaper - we need to decrease price
            return BigDecimal.valueOf(0.9); // 10% decrease
        } else if (ratio > 1.2) {
            // Competitor is significantly more expensive - we can increase price
            return BigDecimal.valueOf(1.1); // 10% increase
        } else {
            // Prices are comparable
            return BigDecimal.ONE;
        }
    }
    
    /**
     * Enum for different types of price factors
     */
    public enum PriceFactorType {
        SEASONAL,           // Seasonal demand variations
        TIME_BASED,         // Time of week, time of day
        DEMAND,             // Current demand levels
        DURATION,           // Rental duration
        LEAD_TIME,          // How far in advance booking is made
        VEHICLE_QUALITY,    // Vehicle rating, age, condition
        VEHICLE_TYPE,       // Type of vehicle (SUV, sedan, etc.)
        LOCATION,           // Geographic location
        HOLIDAY,            // Holiday periods
        COMPETITION,        // Competitive market pricing
        PROMOTIONAL,        // Special promotions and discounts
        LOYALTY,            // Customer loyalty rewards
        VOLUME,             // Volume-based discounts
        URGENCY,            // Urgent booking premium
        FLEXIBILITY,        // Flexible booking options
        PEAK_HOURS,         // Peak usage hours
        WEATHER,            // Weather conditions
        EVENT_BASED,        // Special events in area
        FUEL_PRICE,         // Current fuel prices
        MAINTENANCE,        // Vehicle maintenance status
        INSURANCE,          // Insurance costs
        CUSTOMER_SEGMENT,   // Customer segment pricing
        PAYMENT_METHOD,     // Payment method discounts
        BUNDLE,             // Bundle pricing
        DYNAMIC             // Real-time dynamic adjustments
    }
    
    /**
     * Builder with additional convenience methods
     */
    public static class PriceFactorBuilder {
        
        public PriceFactorBuilder multiplier(double multiplier) {
            this.multiplier = BigDecimal.valueOf(multiplier);
            return this;
        }
        
        public PriceFactorBuilder weight(double weight) {
            this.weight = BigDecimal.valueOf(weight);
            return this;
        }
        
        public PriceFactorBuilder confidence(double confidence) {
            this.confidence = BigDecimal.valueOf(confidence);
            return this;
        }
        
        public PriceFactorBuilder withPercentageChange(double percentageChange) {
            this.multiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(percentageChange / 100));
            return this;
        }
    }
}