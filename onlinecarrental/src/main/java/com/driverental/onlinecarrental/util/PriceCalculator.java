package com.driverental.onlinecarrental.util;

public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static double calculate(double base, double factor) {
        return base * factor;
    }
}
