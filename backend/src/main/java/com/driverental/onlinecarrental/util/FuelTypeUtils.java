package com.driverental.onlinecarrental.util;

import com.driverental.onlinecarrental.model.enums.FuelType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for FuelType operations and calculations
 */
public class FuelTypeUtils {

    private FuelTypeUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculate fuel cost for a given distance and fuel type
     */
    public static double calculateFuelCost(FuelType fuelType, double distance, double fuelPricePerUnit) {
        double consumption = distance / fuelType.getTypicalRangePerUnit();
        return consumption * fuelPricePerUnit * fuelType.getCostFactor();
    }

    /**
     * Calculate carbon emissions for a given distance
     */
    public static double calculateCarbonEmissions(FuelType fuelType, double distance) {
        // CO2 emissions in kg per km
        double emissionsPerKm = switch (fuelType) {
            case PETROL -> 0.24;
            case DIESEL -> 0.22;
            case ELECTRIC -> 0.05; // Depends on electricity source
            case HYBRID -> 0.12;
            case PLUGIN_HYBRID -> 0.08;
            case CNG -> 0.18;
            case LPG -> 0.20;
            case HYDROGEN -> 0.0;  // Only water vapor
            case BIODIESEL -> 0.15;
            case ETHANOL -> 0.17;
            case SYNTHETIC -> 0.23;
        };
        
        return emissionsPerKm * distance;
    }

    /**
     * Get fuel types sorted by environmental impact
     */
    public static List<FuelType> getSortedByEnvironmentalImpact() {
        return Arrays.stream(FuelType.values())
                .sorted(Comparator.comparing(FuelType::getEnvironmentalImpact))
                .collect(Collectors.toList());
    }

    /**
     * Get fuel types sorted by cost efficiency
     */
    public static List<FuelType> getSortedByCostEfficiency() {
        return Arrays.stream(FuelType.values())
                .sorted(Comparator.comparing(FuelType::getCostFactor))
                .collect(Collectors.toList());
    }

    /**
     * Get fuel types suitable for a specific use case
     */
    public static List<FuelType> getFuelTypesForUseCase(String useCase) {
        return switch (useCase.toLowerCase()) {
            case "city" -> Arrays.asList(
                FuelType.ELECTRIC, FuelType.HYBRID, FuelType.PETROL, FuelType.CNG
            );
            case "highway" -> Arrays.asList(
                FuelType.DIESEL, FuelType.PETROL, FuelType.HYBRID, FuelType.PLUGIN_HYBRID
            );
            case "eco" -> FuelType.getEcoFriendlyFuelTypes();
            case "performance" -> Arrays.asList(
                FuelType.PETROL, FuelType.DIESEL, FuelType.ELECTRIC, FuelType.SYNTHETIC
            );
            case "long_distance" -> FuelType.getLongDistanceFuelTypes();
            case "low_cost" -> FuelType.getLowCostFuelTypes();
            default -> Arrays.asList(FuelType.values());
        };
    }

    /**
     * Calculate total operating cost including maintenance
     */
    public static double calculateTotalOperatingCost(FuelType fuelType, double annualDistance, 
                                                   double fuelPrice, double baseMaintenanceCost) {
        double fuelCost = calculateFuelCost(fuelType, annualDistance, fuelPrice);
        double maintenanceCost = baseMaintenanceCost * fuelType.getMaintenanceCostFactor();
        return fuelCost + maintenanceCost;
    }

    /**
     * Get fuel type recommendations based on user preferences
     */
    public static Map<FuelType, Double> getFuelTypeRecommendations(
            boolean prioritizeCost, 
            boolean prioritizeEnvironment, 
            boolean longDistance, 
            boolean cityDriving) {
        
        return Arrays.stream(FuelType.values())
                .collect(Collectors.toMap(
                    fuelType -> fuelType,
                    fuelType -> calculateRecommendationScore(fuelType, prioritizeCost, 
                                                           prioritizeEnvironment, longDistance, cityDriving)
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0.5)
                .sorted(Map.Entry.<FuelType, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    java.util.LinkedHashMap::new
                ));
    }

    private static double calculateRecommendationScore(FuelType fuelType, boolean prioritizeCost, 
                                                     boolean prioritizeEnvironment, 
                                                     boolean longDistance, boolean cityDriving) {
        double score = 0.0;
        
        if (prioritizeCost) {
            score += (1.0 - fuelType.getCostFactor()) * 0.4;
        }
        
        if (prioritizeEnvironment) {
            score += (1.0 - fuelType.getEnvironmentalImpact()) * 0.4;
        }
        
        if (longDistance) {
            score += (fuelType.getAvailabilityScore() / 10.0) * 0.2;
        }
        
        if (cityDriving) {
            if (fuelType.isElectricOrHybrid()) {
                score += 0.2;
            }
        }
        
        return Math.min(score, 1.0);
    }

    /**
     * Get fuel type compatibility matrix
     */
    public static Map<FuelType, List<FuelType>> getCompatibleFuelTypes() {
        return Map.of(
            FuelType.PETROL, Arrays.asList(FuelType.PETROL, FuelType.ETHANOL),
            FuelType.DIESEL, Arrays.asList(FuelType.DIESEL, FuelType.BIODIESEL),
            FuelType.ELECTRIC, List.of(FuelType.ELECTRIC),
            FuelType.HYBRID, Arrays.asList(FuelType.PETROL, FuelType.ELECTRIC),
            FuelType.PLUGIN_HYBRID, Arrays.asList(FuelType.PETROL, FuelType.ELECTRIC),
            FuelType.CNG, List.of(FuelType.CNG),
            FuelType.LPG, List.of(FuelType.LPG),
            FuelType.HYDROGEN, List.of(FuelType.HYDROGEN),
            FuelType.BIODIESEL, Arrays.asList(FuelType.DIESEL, FuelType.BIODIESEL),
            FuelType.ETHANOL, Arrays.asList(FuelType.PETROL, FuelType.ETHANOL),
            FuelType.SYNTHETIC, Arrays.asList(FuelType.PETROL, FuelType.DIESEL)
        );
    }

    /**
     * Check if two fuel types are compatible
     */
    public static boolean areCompatible(FuelType type1, FuelType type2) {
        List<FuelType> compatibleTypes = getCompatibleFuelTypes().get(type1);
        return compatibleTypes != null && compatibleTypes.contains(type2);
    }

    /**
     * Get transition recommendations for switching fuel types
     */
    public static List<FuelType> getTransitionRecommendations(FuelType currentType, 
                                                            FuelType.TargetCategory targetCategory) {
        return Arrays.stream(FuelType.values())
                .filter(targetType -> targetType != currentType)
                .filter(targetType -> isGoodTransition(currentType, targetType, targetCategory))
                .sorted(Comparator.comparingDouble(type -> 
                    calculateTransitionEase(currentType, type)))
                .collect(Collectors.toList());
    }

    private static boolean isGoodTransition(FuelType from, FuelType to, FuelType.TargetCategory category) {
        // Implementation would depend on specific transition logic
        return true;
    }

    private static double calculateTransitionEase(FuelType from, FuelType to) {
        // Simple implementation - in reality this would consider infrastructure, cost, etc.
        if (from == to) return 0.0;
        if (areCompatible(from, to)) return 0.3;
        if (from.isFossilFuel() && to.isFossilFuel()) return 0.5;
        if (from.isElectricOrHybrid() && to.isElectricOrHybrid()) return 0.6;
        return 1.0;
    }

    /**
     * Target categories for fuel type transitions
     */
    public enum TargetCategory {
        COST_REDUCTION,
        ENVIRONMENTAL,
        PERFORMANCE,
        CONVENIENCE
    }
}