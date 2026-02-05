package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.entity.Review;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates similarity between vehicles/items based on various factors:
 * - Vehicle attributes (type, price, seats, transmission, fuel type)
 * - Booking patterns (co-occurrence in user bookings)
 * - Rating patterns (similarity in user ratings)
 * - Feature similarity
 */
@Component
@Slf4j
public class ItemSimilarity {

    // Weight configuration for different similarity factors
    private static final double ATTRIBUTE_SIMILARITY_WEIGHT = 0.4;
    private static final double CO_OCCURRENCE_SIMILARITY_WEIGHT = 0.3;
    private static final double RATING_SIMILARITY_WEIGHT = 0.2;
    private static final double PRICE_SIMILARITY_WEIGHT = 0.1;

    /**
     * Calculate overall similarity between two vehicles
     */
    public double calculateOverallSimilarity(Vehicle vehicle1, Vehicle vehicle2,
                                             List<Booking> allBookings, List<Review> allReviews) {
        if (vehicle1 == null || vehicle2 == null) {
            return 0.0;
        }

        // Avoid comparing vehicle with itself
        if (vehicle1.getId().equals(vehicle2.getId())) {
            return 1.0;
        }

        double attributeSimilarity = calculateAttributeSimilarity(vehicle1, vehicle2);
        double coOccurrenceSimilarity = calculateCoOccurrenceSimilarity(vehicle1, vehicle2, allBookings);
        double ratingSimilarity = calculateRatingSimilarity(vehicle1, vehicle2, allReviews);
        double priceSimilarity = calculatePriceSimilarity(vehicle1, vehicle2);

        double overallSimilarity = (ATTRIBUTE_SIMILARITY_WEIGHT * attributeSimilarity) +
                (CO_OCCURRENCE_SIMILARITY_WEIGHT * coOccurrenceSimilarity) +
                (RATING_SIMILARITY_WEIGHT * ratingSimilarity) +
                (PRICE_SIMILARITY_WEIGHT * priceSimilarity);

        log.debug(
                "Vehicle similarity calculated: {} vs {} = {} (attribute: {}, co-occurrence: {}, rating: {}, price: {})",
                vehicle1.getId(), vehicle2.getId(), overallSimilarity, attributeSimilarity,
                coOccurrenceSimilarity, ratingSimilarity, priceSimilarity);

        return overallSimilarity;
    }

    /**
     * Calculate similarity based on vehicle attributes
     */
    public double calculateAttributeSimilarity(Vehicle vehicle1, Vehicle vehicle2) {
        double similarity = 0.0;
        int factors = 0;

        // Type similarity (exact match = 1.0, different = 0.0)
        if (vehicle1.getType() == vehicle2.getType()) {
            similarity += 1.0;
        }
        factors++;

        // Seats similarity (normalized difference)
        double seatsSimilarity = 1.0 - Math.abs(vehicle1.getSeats() - vehicle2.getSeats()) / Math.max(vehicle1.getSeats(), vehicle2.getSeats());
        similarity += Math.max(0.0, seatsSimilarity);
        factors++;

        // Transmission similarity (exact match = 1.0, different = 0.0)
        if (vehicle1.getTransmission() != null && vehicle2.getTransmission() != null &&
                vehicle1.getTransmission().equals(vehicle2.getTransmission())) {
            similarity += 1.0;
        }
        factors++;

        // Fuel type similarity (exact match = 1.0, different = 0.0)
        if (vehicle1.getFuelType() != null && vehicle2.getFuelType() != null &&
                vehicle1.getFuelType().equals(vehicle2.getFuelType())) {
            similarity += 1.0;
        }
        factors++;

        // Luggage capacity similarity
        if (vehicle1.getLuggageCapacity() != null && vehicle2.getLuggageCapacity() != null) {
            double luggageSimilarity = 1.0 - Math.abs(vehicle1.getLuggageCapacity() - vehicle2.getLuggageCapacity()) /
                    Math.max(vehicle1.getLuggageCapacity(), vehicle2.getLuggageCapacity());
            similarity += Math.max(0.0, luggageSimilarity);
        }
        factors++;

        return factors > 0 ? similarity / factors : 0.0;
    }

    /**
     * Calculate similarity based on co-occurrence in user bookings (Jaccard similarity)
     */
    public double calculateCoOccurrenceSimilarity(Vehicle vehicle1, Vehicle vehicle2, List<Booking> allBookings) {
        if (allBookings == null || allBookings.isEmpty()) {
            return 0.0;
        }

        // Get users who booked each vehicle
        Set<Long> users1 = allBookings.stream()
                .filter(booking -> booking.getVehicle().getId().equals(vehicle1.getId()))
                .map(booking -> booking.getUser().getId())
                .collect(Collectors.toSet());

        Set<Long> users2 = allBookings.stream()
                .filter(booking -> booking.getVehicle().getId().equals(vehicle2.getId()))
                .map(booking -> booking.getUser().getId())
                .collect(Collectors.toSet());

        if (users1.isEmpty() && users2.isEmpty()) {
            return 0.5; // Neutral similarity if no bookings
        }
        if (users1.isEmpty() || users2.isEmpty()) {
            return 0.0;
        }

        // Calculate Jaccard similarity
        Set<Long> intersection = new HashSet<>(users1);
        intersection.retainAll(users2);

        Set<Long> union = new HashSet<>(users1);
        union.addAll(users2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Calculate similarity based on rating patterns (Pearson correlation)
     */
    public double calculateRatingSimilarity(Vehicle vehicle1, Vehicle vehicle2, List<Review> allReviews) {
        if (allReviews == null || allReviews.isEmpty()) {
            return 0.0;
        }

        // Get ratings by user for each vehicle
        Map<Long, Integer> ratings1 = allReviews.stream()
                .filter(review -> review.getVehicle().getId().equals(vehicle1.getId()))
                .collect(Collectors.toMap(
                        review -> review.getUser().getId(),
                        Review::getRating,
                        (existing, replacement) -> existing)); // Keep first rating if duplicate

        Map<Long, Integer> ratings2 = allReviews.stream()
                .filter(review -> review.getVehicle().getId().equals(vehicle2.getId()))
                .collect(Collectors.toMap(
                        review -> review.getUser().getId(),
                        Review::getRating,
                        (existing, replacement) -> existing));

        if (ratings1.isEmpty() && ratings2.isEmpty()) {
            return 0.5;
        }
        if (ratings1.isEmpty() || ratings2.isEmpty()) {
            return 0.0;
        }

        // Find common users who rated both vehicles
        Set<Long> commonUsers = new HashSet<>(ratings1.keySet());
        commonUsers.retainAll(ratings2.keySet());

        if (commonUsers.size() < 2) {
            return 0.0; // Need at least 2 common users for correlation
        }

        // Calculate Pearson correlation
        double correlation = calculatePearsonCorrelation(ratings1, ratings2, commonUsers);

        // Normalize correlation to 0-1 range
        return Math.max(0.0, (correlation + 1) / 2);
    }

    /**
     * Calculate similarity based on price range
     */
    public double calculatePriceSimilarity(Vehicle vehicle1, Vehicle vehicle2) {
        if (vehicle1.getDailyPrice() == null || vehicle2.getDailyPrice() == null) {
            return 0.0;
        }

        double price1 = vehicle1.getDailyPrice().doubleValue();
        double price2 = vehicle2.getDailyPrice().doubleValue();

        double maxPrice = Math.max(price1, price2);
        if (maxPrice == 0) {
            return 1.0; // Both are free
        }

        double priceDifference = Math.abs(price1 - price2);
        return 1.0 - Math.min(1.0, priceDifference / maxPrice);
    }

    /**
     * Find the top N most similar vehicles to a target vehicle
     */
    public List<VehicleSimilarityScore> findSimilarVehicles(Vehicle targetVehicle, List<Vehicle> allVehicles,
                                                             List<Booking> allBookings, List<Review> allReviews, int topN) {
        return allVehicles.stream()
                .filter(vehicle -> !vehicle.getId().equals(targetVehicle.getId()))
                .filter(Vehicle::getIsAvailable)
                .map(vehicle -> new VehicleSimilarityScore(
                        vehicle,
                        calculateOverallSimilarity(targetVehicle, vehicle, allBookings, allReviews)))
                .sorted(Comparator.comparing(VehicleSimilarityScore::getSimilarityScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculate Pearson correlation coefficient between two rating maps
     */
    private double calculatePearsonCorrelation(Map<Long, Integer> ratings1, Map<Long, Integer> ratings2,
                                               Set<Long> commonItems) {
        if (commonItems.size() < 2) {
            return 0.0;
        }

        double sum1 = 0.0, sum2 = 0.0;
        double sum1Sq = 0.0, sum2Sq = 0.0;
        double pSum = 0.0;

        for (Long userId : commonItems) {
            int rating1 = ratings1.get(userId);
            int rating2 = ratings2.get(userId);

            sum1 += rating1;
            sum2 += rating2;
            sum1Sq += Math.pow(rating1, 2);
            sum2Sq += Math.pow(rating2, 2);
            pSum += rating1 * rating2;
        }

        int n = commonItems.size();
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / n) * (sum2Sq - Math.pow(sum2, 2) / n));

        if (den == 0) {
            return 0.0;
        }

        return num / den;
    }

    /**
     * Data class to hold vehicle similarity scores
     */
    @Data
    public static class VehicleSimilarityScore {
        private final Vehicle vehicle;
        private final double similarityScore;

        public VehicleSimilarityScore(Vehicle vehicle, double similarityScore) {
            this.vehicle = vehicle;
            this.similarityScore = similarityScore;
        }

        @Override
        public String toString() {
            return String.format("VehicleSimilarityScore{vehicle=%d, similarity=%.3f}",
                    vehicle.getId(), similarityScore);
        }
    }
}
