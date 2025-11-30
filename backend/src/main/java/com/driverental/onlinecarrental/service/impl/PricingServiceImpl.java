package com.driverental.onlinecarrental.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    @DisplayName("Calculate total price for valid date range")
    void calculateTotalPrice_ValidRange_ReturnsCorrectPrice() {
        BigDecimal dailyRate = new BigDecimal("50.00");
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = startDate.plusDays(5);
        
        BigDecimal totalPrice = PriceCalculator.calculateTotalPrice(dailyRate, startDate, endDate);
        
        assertEquals(new BigDecimal("250.00"), totalPrice);
    }

    @Test
    @DisplayName("Apply seasonal pricing during peak season")
    void applySeasonalPricing_PeakSeason_AppliesMultiplier() {
        BigDecimal price = new BigDecimal("100.00");
        LocalDate peakSeasonDate = LocalDate.of(2024, 7, 15); // July (peak season)
        
        BigDecimal adjustedPrice = PriceCalculator.applySeasonalPricing(price, peakSeasonDate);
        
        assertEquals(new BigDecimal("130.00"), adjustedPrice);
    }

    @Test
    @DisplayName("Calculate cancellation fee for different notice periods")
    void calculateCancellationFee_VariousNoticePeriods_ReturnsCorrectFees() {
        BigDecimal totalPrice = new BigDecimal("200.00");
        
        // Same day cancellation
        LocalDate sameDayStart = LocalDate.now().plusDays(1);
        BigDecimal sameDayFee = PriceCalculator.calculateCancellationFee(totalPrice, sameDayStart, LocalDate.now());
        assertEquals(new BigDecimal("200.00"), sameDayFee);
        
        // 2 days notice
        LocalDate twoDaysStart = LocalDate.now().plusDays(3);
        BigDecimal twoDaysFee = PriceCalculator.calculateCancellationFee(totalPrice, twoDaysStart, LocalDate.now());
        assertEquals(new BigDecimal("150.00"), twoDaysFee);
        
        // 14+ days notice - no fee
        LocalDate longNoticeStart = LocalDate.now().plusDays(15);
        BigDecimal longNoticeFee = PriceCalculator.calculateCancellationFee(totalPrice, longNoticeStart, LocalDate.now());
        assertEquals(BigDecimal.ZERO, longNoticeFee);
    }

    @Test
    @DisplayName("Calculate insurance cost for different types")
    void calculateInsuranceCost_DifferentTypes_ReturnsCorrectCosts() {
        BigDecimal totalPrice = new BigDecimal("1000.00");
        
        BigDecimal premiumInsurance = PriceCalculator.calculateInsuranceCost(totalPrice, "PREMIUM");
        BigDecimal standardInsurance = PriceCalculator.calculateInsuranceCost(totalPrice, "STANDARD");
        BigDecimal basicInsurance = PriceCalculator.calculateInsuranceCost(totalPrice, "BASIC");
        BigDecimal noInsurance = PriceCalculator.calculateInsuranceCost(totalPrice, "NONE");
        
        assertEquals(new BigDecimal("150.00"), premiumInsurance);
        assertEquals(new BigDecimal("100.00"), standardInsurance);
        assertEquals(new BigDecimal("50.00"), basicInsurance);
        assertEquals(BigDecimal.ZERO, noInsurance);
    }

    @Test
    @DisplayName("Calculate additional fees for services")
    void calculateAdditionalFees_Services_ReturnsCorrectTotal() {
        String[] services = {"DELIVERY", "CLEANING", "GPS"};
        
        BigDecimal totalFees = PriceCalculator.calculateAdditionalFees(services);
        
        assertEquals(new BigDecimal("43.00"), totalFees); // 25 + 15 + 3
    }
}