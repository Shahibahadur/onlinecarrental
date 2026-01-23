package com.driverental.onlinecarrental.model.enums;

public enum ImageCategory {
    SEDAN("Sedan"),
    SPORTS("Sports"),
    CONVERTIBLE("Convertible"),
    HATCHBACK("Hatchback"),
    HYBRID("Hybrid"),
    ELECTRIC("Electric"),
    SUV("SUV");

    private final String description;

    ImageCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
