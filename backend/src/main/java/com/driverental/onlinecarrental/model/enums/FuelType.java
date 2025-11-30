package com.driverental.onlinecarrental.model.enums;

import lombok.Getter;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enum representing different types of vehicle fuel/energy sources
 * Includes traditional fuels, alternative fuels, and electric power
 */
@Getter
public enum FuelType {
    
    /**
     * Petrol/Gasoline - Most common fuel for internal combustion engines
     */
    PETROL(
        "Petrol", 
        "Gasoline", 
        "petrol",
        "L",
        "#FF6B35",
        0.12, // Relative cost factor
        0.08, // Environmental impact factor (higher is worse)
        "Internal Combustion Engine"
    ),
    
    /**
     * Diesel - Higher efficiency fuel for compression ignition engines
     */
    DIESEL(
        "Diesel", 
        "Diesel", 
        "diesel",
        "L",
        "#004E89",
        0.10, // Relative cost factor
        0.09, // Environmental impact factor
        "Compression Ignition Engine"
    ),
    
    /**
     * Electric - Battery electric vehicles
     */
    ELECTRIC(
        "Electric", 
        "Electric", 
        "electric",
        "kWh",
        "#00A896",
        0.05, // Relative cost factor
        0.01, // Environmental impact factor
        "Electric Motor"
    ),
    
    /**
     * Hybrid - Combination of petrol engine and electric motor
     */
    HYBRID(
        "Hybrid", 
        "Hybrid", 
        "hybrid",
        "L/kWh",
        "#6A8EAE",
        0.08, // Relative cost factor
        0.04, // Environmental impact factor
        "Hybrid System"
    ),
    
    /**
     * Plug-in Hybrid - Hybrid with larger battery that can be plugged in
     */
    PLUGIN_HYBRID(
        "Plug-in Hybrid", 
        "PHEV", 
        "plugin_hybrid",
        "L/kWh",
        "#3D5A80",
        0.07, // Relative cost factor
        0.03, // Environmental impact factor
        "Plug-in Hybrid System"
    ),
    
    /**
     * Compressed Natural Gas - Alternative gaseous fuel
     */
    CNG(
        "Compressed Natural Gas", 
        "CNG", 
        "cng",
        "kg",
        "#7DCFB6",
        0.06, // Relative cost factor
        0.05, // Environmental impact factor
        "Natural Gas Engine"
    ),
    
    /**
     * Liquefied Petroleum Gas - Propane/butane mixture
     */
    LPG(
        "Liquefied Petroleum Gas", 
        "LPG", 
        "lpg",
        "L",
        "#F79256",
        0.05, // Relative cost factor
        0.06, // Environmental impact factor
        "LPG Engine"
    ),
    
    /**
     * Hydrogen Fuel Cell - Electric vehicle powered by hydrogen
     */
    HYDROGEN(
        "Hydrogen Fuel Cell", 
        "Hydrogen", 
        "hydrogen",
        "kg",
        "#9C89B8",
        0.15, // Relative cost factor
        0.02, // Environmental impact factor
        "Fuel Cell System"
    ),
    
    /**
     * Bio-diesel - Renewable diesel alternative
     */
    BIODIESEL(
        "Bio-diesel", 
        "Bio-diesel", 
        "biodiesel",
        "L",
        "#90BE6D",
        0.11, // Relative cost factor
        0.04, // Environmental impact factor
        "Bio-fuel Engine"
    ),
    
    /**
     * Ethanol - Alcohol-based biofuel
     */
    ETHANOL(
        "Ethanol", 
        "Ethanol", 
        "ethanol",
        "L",
        "#F9C74F",
        0.09, // Relative cost factor
        0.05, // Environmental impact factor
        "Flex-fuel Engine"
    ),
    
    /**
     * Synthetic Fuel - Artificially produced fuel
     */
    SYNTHETIC(
        "Synthetic Fuel", 
        "Synthetic", 
        "synthetic",
        "L",
        "#577590",
        0.20, // Relative cost factor
        0.07, // Environmental impact factor
        "Synthetic Fuel Engine"
    );

    // Instance fields
    private final String displayName;
    private final String abbreviation;
    private final String code;
    private final String unit;
    private final String colorCode;
    private final double costFactor;
    private final double environmentalImpact;
    private final String engineType;

    // Cache for faster lookups
    private static final Map<String, FuelType> DISPLAY_NAME_MAP = 
        Arrays.stream(values())
              .collect(Collectors.toMap(FuelType::getDisplayName, Function.identity()));
    
    private static final Map<String, FuelType> CODE_MAP = 
        Arrays.stream(values())
              .collect(Collectors.toMap(FuelType::getCode, Function.identity()));
    
    private static final Map<String, FuelType> ABBREVIATION_MAP = 
        Arrays.stream(values())
              .collect(Collectors.toMap(FuelType::getAbbreviation, Function.identity()));

    /**
     * Constructor
     */
    FuelType(String displayName, String abbreviation, String code, String unit, 
            String colorCode, double costFactor, double environmentalImpact, String engineType) {
        this.displayName = displayName;
        this.abbreviation = abbreviation;
        this.code = code;
        this.unit = unit;
        this.colorCode = colorCode;
        this.costFactor = costFactor;
        this.environmentalImpact = environmentalImpact;
        this.engineType = engineType;
    }

    /**
     * Get FuelType from display name
     */
    public static FuelType fromDisplayName(String displayName) {
        FuelType type = DISPLAY_NAME_MAP.get(displayName);
        if (type == null) {
            throw new IllegalArgumentException("Unknown fuel type display name: " + displayName);
        }
        return type;
    }

    /**
     * Get FuelType from code
     */
    public static FuelType fromCode(String code) {
        FuelType type = CODE_MAP.get(code.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("Unknown fuel type code: " + code);
        }
        return type;
    }

    /**
     * Get FuelType from abbreviation
     */
    public static FuelType fromAbbreviation(String abbreviation) {
        FuelType type = ABBREVIATION_MAP.get(abbreviation.toUpperCase());
        if (type == null) {
            throw new IllegalArgumentException("Unknown fuel type abbreviation: " + abbreviation);
        }
        return type;
    }

    /**
     * Safe conversion from string with fallback
     */
    public static FuelType fromString(String value, FuelType defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            // Try by enum name first
            return FuelType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e1) {
            try {
                // Try by display name
                return fromDisplayName(value);
            } catch (IllegalArgumentException e2) {
                try {
                    // Try by code
                    return fromCode(value);
                } catch (IllegalArgumentException e3) {
                    try {
                        // Try by abbreviation
                        return fromAbbreviation(value);
                    } catch (IllegalArgumentException e4) {
                        return defaultValue;
                    }
                }
            }
        }
    }

    /**
     * Check if this fuel type is electric/hybrid
     */
    public boolean isElectricOrHybrid() {
        return this == ELECTRIC || this == HYBRID || this == PLUGIN_HYBRID || this == HYDROGEN;
    }

    /**
     * Check if this fuel type is fully electric
     */
    public boolean isFullyElectric() {
        return this == ELECTRIC || this == HYDROGEN;
    }

    /**
     * Check if this fuel type is hybrid
     */
    public boolean isHybrid() {
        return this == HYBRID || this == PLUGIN_HYBRID;
    }

    /**
     * Check if this fuel type is alternative fuel
     */
    public boolean isAlternativeFuel() {
        return this == CNG || this == LPG || this == HYDROGEN || 
               this == BIODIESEL || this == ETHANOL || this == SYNTHETIC;
    }

    /**
     * Check if this fuel type is biofuel
     */
    public boolean isBiofuel() {
        return this == BIODIESEL || this == ETHANOL;
    }

    /**
     * Check if this fuel type is fossil fuel
     */
    public boolean isFossilFuel() {
        return this == PETROL || this == DIESEL;
    }

    /**
     * Check if this fuel type requires charging
     */
    public boolean requiresCharging() {
        return this == ELECTRIC || this == PLUGIN_HYBRID;
    }

    /**
     * Check if this fuel type requires refueling
     */
    public boolean requiresRefueling() {
        return !isFullyElectric();
    }

    /**
     * Get environmental impact category
     */
    public EnvironmentalImpact getEnvironmentalImpactCategory() {
        if (environmentalImpact <= 0.02) {
            return EnvironmentalImpact.VERY_LOW;
        } else if (environmentalImpact <= 0.04) {
            return EnvironmentalImpact.LOW;
        } else if (environmentalImpact <= 0.07) {
            return EnvironmentalImpact.MODERATE;
        } else if (environmentalImpact <= 0.09) {
            return EnvironmentalImpact.HIGH;
        } else {
            return EnvironmentalImpact.VERY_HIGH;
        }
    }

    /**
     * Get cost efficiency rating (1-5 stars)
     */
    public int getCostEfficiencyStars() {
        if (costFactor <= 0.06) {
            return 5; // ⭐⭐⭐⭐⭐
        } else if (costFactor <= 0.08) {
            return 4; // ⭐⭐⭐⭐
        } else if (costFactor <= 0.10) {
            return 3; // ⭐⭐⭐
        } else if (costFactor <= 0.12) {
            return 2; // ⭐⭐
        } else {
            return 1; // ⭐
        }
    }

    /**
     * Get recommended vehicle types for this fuel type
     */
    public VehicleType[] getRecommendedVehicleTypes() {
        return switch (this) {
            case ELECTRIC, HYDROGEN -> new VehicleType[]{
                VehicleType.SEDAN, VehicleType.HATCHBACK, VehicleType.SUV, VehicleType.LUXURY
            };
            case HYBRID, PLUGIN_HYBRID -> new VehicleType[]{
                VehicleType.SEDAN, VehicleType.SUV, VehicleType.HATCHBACK
            };
            case DIESEL -> new VehicleType[]{
                VehicleType.SUV, VehicleType.LUXURY, VehicleType.SPORTS
            };
            case PETROL -> VehicleType.values(); // All types
            case CNG, LPG -> new VehicleType[]{
                VehicleType.SEDAN, VehicleType.HATCHBACK
            };
            case BIODIESEL, ETHANOL -> new VehicleType[]{
                VehicleType.SEDAN, VehicleType.SUV
            };
            case SYNTHETIC -> new VehicleType[]{
                VehicleType.LUXURY, VehicleType.SPORTS
            };
        };
    }

    /**
     * Get typical range per unit (km per unit)
     */
    public double getTypicalRangePerUnit() {
        return switch (this) {
            case PETROL -> 12.5;    // km/L
            case DIESEL -> 15.0;    // km/L
            case ELECTRIC -> 6.0;   // km/kWh
            case HYBRID -> 20.0;    // km/L equivalent
            case PLUGIN_HYBRID -> 25.0; // km/L equivalent
            case CNG -> 20.0;       // km/kg
            case LPG -> 10.0;       // km/L
            case HYDROGEN -> 80.0;  // km/kg
            case BIODIESEL -> 14.0; // km/L
            case ETHANOL -> 8.0;    // km/L
            case SYNTHETIC -> 11.0; // km/L
        };
    }

    /**
     * Get typical refueling/charging time in minutes
     */
    public int getTypicalRefuelingTime() {
        return switch (this) {
            case PETROL, DIESEL, LPG, BIODIESEL, ETHANOL, SYNTHETIC -> 5;    // minutes
            case CNG -> 10;    // minutes
            case HYDROGEN -> 5; // minutes
            case ELECTRIC -> 30; // minutes for fast charging
            case HYBRID -> 5;   // minutes
            case PLUGIN_HYBRID -> 120; // minutes for full charge
        };
    }

    /**
     * Get availability score (1-10)
     */
    public int getAvailabilityScore() {
        return switch (this) {
            case PETROL, DIESEL -> 10;
            case ELECTRIC -> 7;
            case HYBRID, PLUGIN_HYBRID -> 8;
            case LPG -> 6;
            case CNG -> 4;
            case HYDROGEN -> 3;
            case BIODIESEL, ETHANOL -> 5;
            case SYNTHETIC -> 2;
        };
    }

    /**
     * Get maintenance cost factor
     */
    public double getMaintenanceCostFactor() {
        return switch (this) {
            case ELECTRIC -> 0.7;  // Lower maintenance
            case HYDROGEN -> 0.8;  // Lower maintenance
            case HYBRID, PLUGIN_HYBRID -> 0.9;  // Slightly lower
            case PETROL, DIESEL -> 1.0;  // Standard
            case CNG, LPG -> 1.1;  // Slightly higher
            case BIODIESEL, ETHANOL -> 1.2;  // Higher
            case SYNTHETIC -> 1.3; // Highest
        };
    }

    /**
     * Get fuel types by category
     */
    public static Map<String, List<FuelType>> getFuelTypesByCategory() {
        return Map.of(
            "Traditional", Arrays.asList(PETROL, DIESEL),
            "Electric & Hybrid", Arrays.asList(ELECTRIC, HYBRID, PLUGIN_HYBRID, HYDROGEN),
            "Alternative Fuels", Arrays.asList(CNG, LPG, BIODIESEL, ETHANOL, SYNTHETIC)
        );
    }

    /**
     * Get eco-friendly fuel types
     */
    public static List<FuelType> getEcoFriendlyFuelTypes() {
        return Arrays.stream(values())
                .filter(type -> type.getEnvironmentalImpactCategory().getScore() <= 2)
                .collect(Collectors.toList());
    }

    /**
     * Get fuel types suitable for long distance
     */
    public static List<FuelType> getLongDistanceFuelTypes() {
        return Arrays.stream(values())
                .filter(type -> type.getAvailabilityScore() >= 7)
                .collect(Collectors.toList());
    }

    /**
     * Get fuel types with low operating cost
     */
    public static List<FuelType> getLowCostFuelTypes() {
        return Arrays.stream(values())
                .filter(type -> type.getCostEfficiencyStars() >= 4)
                .collect(Collectors.toList());
    }

    /**
     * Convert to simple string representation for forms
     */
    public String toFormValue() {
        return this.code;
    }

    /**
     * Get icon name for UI display
     */
    public String getIconName() {
        return switch (this) {
            case PETROL -> "fuel-pump";
            case DIESEL -> "fuel-pump-diesel";
            case ELECTRIC -> "lightning-charge";
            case HYBRID -> "gear-fill";
            case PLUGIN_HYBRID -> "plug";
            case CNG -> "cloud-fill";
            case LPG -> "fire";
            case HYDROGEN -> "droplet-half";
            case BIODIESEL -> "tree-fill";
            case ETHANOL -> "flower1";
            case SYNTHETIC -> "gear-wide-connected";
        };
    }

    /**
     * Environmental impact categories
     */
    public enum EnvironmentalImpact {
        VERY_LOW(1, "Very Low", "#00C851"),
        LOW(2, "Low", "#8BC34A"),
        MODERATE(3, "Moderate", "#FFC107"),
        HIGH(4, "High", "#FF9800"),
        VERY_HIGH(5, "Very High", "#F44336");

        private final int score;
        private final String description;
        private final String color;

        EnvironmentalImpact(int score, String description, String color) {
            this.score = score;
            this.description = description;
            this.color = color;
        }

        public int getScore() {
            return score;
        }

        public String getDescription() {
            return description;
        }

        public String getColor() {
            return color;
        }
    }

    /**
     * Fuel efficiency categories
     */
    public enum EfficiencyCategory {
        EXCELLENT(5, "Excellent", "More than 20 km/L"),
        GOOD(4, "Good", "15-20 km/L"),
        AVERAGE(3, "Average", "10-15 km/L"),
        POOR(2, "Poor", "5-10 km/L"),
        VERY_POOR(1, "Very Poor", "Less than 5 km/L");

        private final int rating;
        private final String description;
        private final String range;

        EfficiencyCategory(int rating, String description, String range) {
            this.rating = rating;
            this.description = description;
            this.range = range;
        }

        public int getRating() {
            return rating;
        }

        public String getDescription() {
            return description;
        }

        public String getRange() {
            return range;
        }
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}