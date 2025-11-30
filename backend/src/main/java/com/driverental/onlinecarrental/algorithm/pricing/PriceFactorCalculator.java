package com.driverental.onlinecarrental.algorithm.pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for calculating and combining price factors
 */
@Component
@Slf4j
public class PriceFactorCalculator {
    
    private static final BigDecimal MIN_MULTIPLIER = BigDecimal.valueOf(0.5);
    private static final BigDecimal MAX_MULTIPLIER = BigDecimal.valueOf(2.0);
    private static final BigDecimal WEIGHT_THRESHOLD = BigDecimal.valueOf(0.1);
    
    /**
     * Calculate the combined multiplier from multiple price factors
     */
    public BigDecimal calculateCombinedMultiplier(List<PriceFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return BigDecimal.ONE;
        }
        
        // Filter out factors with very low weight or confidence
        List<PriceFactor> significantFactors = factors.stream()
                .filter(factor -> factor.getWeight().compareTo(WEIGHT_THRESHOLD) >= 0)
                .filter(factor -> factor.getConfidence().compareTo(BigDecimal.valueOf(0.3)) >= 0)
                .sorted() // Sort by weight (descending)
                .collect(Collectors.toList());
        
        if (significantFactors.isEmpty()) {
            return BigDecimal.ONE;
        }
        
        // Calculate weighted geometric mean for multipliers
        BigDecimal weightedProduct = BigDecimal.ONE;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (PriceFactor factor : significantFactors) {
            BigDecimal adjustedWeight = factor.getWeight().multiply(factor.getConfidence());
            BigDecimal weightedMultiplier = BigDecimal.valueOf(
                Math.pow(factor.getMultiplier().doubleValue(), adjustedWeight.doubleValue())
            );
            weightedProduct = weightedProduct.multiply(weightedMultiplier);
            totalWeight = totalWeight.add(adjustedWeight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        
        BigDecimal combinedMultiplier = weightedProduct;
        
        // Apply bounds to prevent extreme pricing
        combinedMultiplier = applyMultiplierBounds(combinedMultiplier);
        
        log.debug("Combined multiplier calculated: {} from {} significant factors", 
                 combinedMultiplier, significantFactors.size());
        
        return combinedMultiplier.setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate final price using multiple factors
     */
    public BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PriceFactor> factors) {
        BigDecimal combinedMultiplier = calculateCombinedMultiplier(factors);
        BigDecimal finalPrice = basePrice.multiply(combinedMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
        
        log.info("Final price calculation: base={}, multiplier={}, final={}", 
                basePrice, combinedMultiplier, finalPrice);
        
        return finalPrice;
    }
    
    /**
     * Analyze the impact of each factor on the final price
     */
    public Map<String, Object> analyzePriceFactors(BigDecimal basePrice, List<PriceFactor> factors) {
        Map<String, Object> analysis = new LinkedHashMap<>();
        
        analysis.put("basePrice", basePrice);
        analysis.put("totalFactors", factors.size());
        
        // Group factors by type
        Map<PriceFactor.PriceFactorType, List<PriceFactor>> factorsByType = factors.stream()
                .collect(Collectors.groupingBy(PriceFactor::getType));
        
        analysis.put("factorsByType", factorsByType);
        
        // Calculate impact of each factor
        List<Map<String, Object>> factorImpacts = factors.stream()
                .map(factor -> {
                    Map<String, Object> impact = new HashMap<>();
                    impact.put("name", factor.getName());
                    impact.put("type", factor.getType());
                    impact.put("multiplier", factor.getMultiplier());
                    impact.put("weight", factor.getWeight());
                    impact.put("confidence", factor.getConfidence());
                    impact.put("percentageChange", factor.getPercentageChange());
                    impact.put("absoluteImpact", factor.calculateImpact(basePrice));
                    impact.put("description", factor.getDescription());
                    return impact;
                })
                .collect(Collectors.toList());
        
        analysis.put("factorImpacts", factorImpacts);
        
        // Calculate summary statistics
        BigDecimal totalIncrease = BigDecimal.ZERO;
        BigDecimal totalDecrease = BigDecimal.ZERO;
        int increaseCount = 0;
        int decreaseCount = 0;
        
        for (PriceFactor factor : factors) {
            BigDecimal impact = factor.calculateImpact(basePrice);
            if (factor.isPriceIncrease()) {
                totalIncrease = totalIncrease.add(impact);
                increaseCount++;
            } else if (factor.isPriceDecrease()) {
                totalDecrease = totalDecrease.add(impact);
                decreaseCount++;
            }
        }
        
        analysis.put("totalPriceIncrease", totalIncrease);
        analysis.put("totalPriceDecrease", totalDecrease);
        analysis.put("increaseFactorsCount", increaseCount);
        analysis.put("decreaseFactorsCount", decreaseCount);
        analysis.put("netPriceChange", totalIncrease.add(totalDecrease));
        
        return analysis;
    }
    
    /**
     * Optimize factors for maximum revenue while maintaining competitiveness
     */
    public List<PriceFactor> optimizeFactors(List<PriceFactor> factors, 
                                           BigDecimal competitivenessThreshold,
                                           BigDecimal maxPriceIncrease) {
        List<PriceFactor> optimized = new ArrayList<>(factors);
        
        // Reduce factors that make price uncompetitive
        optimized = optimized.stream()
                .map(factor -> {
                    if (factor.isPriceIncrease() && 
                        factor.getMultiplier().compareTo(competitivenessThreshold) > 0) {
                        // Reduce the multiplier for competitiveness
                        BigDecimal reducedMultiplier = competitivenessThreshold
                                .add(factor.getMultiplier().subtract(competitivenessThreshold)
                                .multiply(BigDecimal.valueOf(0.5)));
                        
                        return factor.toBuilder()
                                .multiplier(reducedMultiplier)
                                .description(factor.getDescription() + " (optimized for competitiveness)")
                                .build();
                    }
                    return factor;
                })
                .collect(Collectors.toList());
        
        // Cap individual factor impacts
        optimized = optimized.stream()
                .map(factor -> {
                    if (factor.isPriceIncrease() && 
                        factor.getMultiplier().compareTo(BigDecimal.ONE.add(maxPriceIncrease)) > 0) {
                        return factor.toBuilder()
                                .multiplier(BigDecimal.ONE.add(maxPriceIncrease))
                                .description(factor.getDescription() + " (capped at max increase)")
                                .build();
                    }
                    return factor;
                })
                .collect(Collectors.toList());
        
        return optimized;
    }
    
    /**
     * Validate price factors for consistency and reasonable values
     */
    public List<String> validateFactors(List<PriceFactor> factors) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < factors.size(); i++) {
            PriceFactor factor = factors.get(i);
            
            // Check multiplier bounds
            if (factor.getMultiplier().compareTo(MIN_MULTIPLIER) < 0) {
                errors.add(String.format("Factor %d (%s) has multiplier below minimum: %s", 
                        i, factor.getName(), factor.getMultiplier()));
            }
            if (factor.getMultiplier().compareTo(MAX_MULTIPLIER) > 0) {
                errors.add(String.format("Factor %d (%s) has multiplier above maximum: %s", 
                        i, factor.getName(), factor.getMultiplier()));
            }
            
            // Check weight bounds
            if (factor.getWeight().compareTo(BigDecimal.ZERO) < 0 || 
                factor.getWeight().compareTo(BigDecimal.ONE) > 0) {
                errors.add(String.format("Factor %d (%s) has invalid weight: %s", 
                        i, factor.getName(), factor.getWeight()));
            }
            
            // Check confidence bounds
            if (factor.getConfidence().compareTo(BigDecimal.ZERO) < 0 || 
                factor.getConfidence().compareTo(BigDecimal.ONE) > 0) {
                errors.add(String.format("Factor %d (%s) has invalid confidence: %s", 
                        i, factor.getName(), factor.getConfidence()));
            }
        }
        
        // Check for conflicting factors
        List<PriceFactor> increasingFactors = factors.stream()
                .filter(PriceFactor::isPriceIncrease)
                .collect(Collectors.toList());
        
        List<PriceFactor> decreasingFactors = factors.stream()
                .filter(PriceFactor::isPriceDecrease)
                .collect(Collectors.toList());
        
        if (!increasingFactors.isEmpty() && !decreasingFactors.isEmpty()) {
            log.warn("Found both increasing and decreasing factors in the same calculation");
        }
        
        return errors;
    }
    
    /**
     * Create a default set of factors for a standard booking
     */
    public List<PriceFactor> createDefaultFactors(LocalDate startDate, LocalDate endDate, 
                                                 String location, String vehicleType) {
        List<PriceFactor> factors = new ArrayList<>();
        
        long rentalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        // Seasonal factor
        factors.add(PriceFactor.createSeasonalFactor(
            startDate.getMonth(), BigDecimal.valueOf(0.15)));
        
        // Weekend factor for each day in the rental period
        LocalDate currentDate = startDate;
        int weekendDays = 0;
        int totalDays = 0;
        
        while (!currentDate.isAfter(endDate)) {
            if (isWeekend(currentDate)) {
                weekendDays++;
            }
            totalDays++;
            currentDate = currentDate.plusDays(1);
        }
        
        if (totalDays > 0) {
            double weekendRatio = (double) weekendDays / totalDays;
            BigDecimal weekendWeight = BigDecimal.valueOf(weekendRatio * 0.1);
            factors.add(PriceFactor.createWeekendFactor(startDate, weekendWeight));
        }
        
        // Duration factor
        factors.add(PriceFactor.createDurationFactor(
            rentalDays, BigDecimal.valueOf(0.2)));
        
        // Lead time factor
        factors.add(PriceFactor.createLeadTimeFactor(
            startDate, BigDecimal.valueOf(0.15)));
        
        // Location factor
        factors.add(PriceFactor.createLocationFactor(
            location, BigDecimal.valueOf(0.1)));
        
        // Vehicle type factor
        factors.add(PriceFactor.createVehicleTypeFactor(
            vehicleType, BigDecimal.valueOf(0.15)));
        
        // Validate the factors
        List<String> validationErrors = validateFactors(factors);
        if (!validationErrors.isEmpty()) {
            log.warn("Validation errors in default factors: {}", validationErrors);
        }
        
        return factors;
    }
    
    private BigDecimal applyMultiplierBounds(BigDecimal multiplier) {
        if (multiplier.compareTo(MIN_MULTIPLIER) < 0) {
            return MIN_MULTIPLIER;
        }
        if (multiplier.compareTo(MAX_MULTIPLIER) > 0) {
            return MAX_MULTIPLIER;
        }
        return multiplier;
    }
    
    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.FRIDAY || 
               day == java.time.DayOfWeek.SATURDAY || 
               day == java.time.DayOfWeek.SUNDAY;
    }
    
    /**
     * Calculate the sensitivity of price to changes in specific factors
     */
    public Map<String, BigDecimal> calculatePriceSensitivity(BigDecimal basePrice, 
                                                           List<PriceFactor> factors,
                                                           String... factorTypesToTest) {
        Map<String, BigDecimal> sensitivity = new HashMap<>();
        
        BigDecimal currentPrice = calculateFinalPrice(basePrice, factors);
        
        for (String factorType : factorTypesToTest) {
            // Create a modified factors list with this factor increased by 10%
            List<PriceFactor> modifiedFactors = factors.stream()
                    .map(factor -> {
                        if (factor.getType().name().equalsIgnoreCase(factorType)) {
                            BigDecimal modifiedMultiplier = factor.getMultiplier()
                                    .multiply(BigDecimal.valueOf(1.1));
                            return factor.toBuilder()
                                    .multiplier(modifiedMultiplier)
                                    .build();
                        }
                        return factor;
                    })
                    .collect(Collectors.toList());
            
            BigDecimal modifiedPrice = calculateFinalPrice(basePrice, modifiedFactors);
            BigDecimal priceChange = modifiedPrice.subtract(currentPrice);
            BigDecimal sensitivityScore = priceChange.divide(currentPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            sensitivity.put(factorType, sensitivityScore);
        }
        
        return sensitivity;
    }
}