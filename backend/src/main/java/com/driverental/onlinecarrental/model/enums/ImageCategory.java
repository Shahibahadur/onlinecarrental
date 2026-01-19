package com.driverental.onlinecarrental.model.enums;

public enum ImageCategory {
    MAIN("Main Display Image"),
    EXTERIOR("Exterior Views"),
    INTERIOR("Interior Views"),
    FEATURES("Feature Highlights"),
    SAFETY("Safety Features"),
    AMENITIES("Amenities"),
    PERFORMANCE("Performance & Specs");

    private final String description;

    ImageCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
