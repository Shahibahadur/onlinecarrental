package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.Review;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.enum.VehicleType;
import com.driverental.onlinecarrental.model.enum.FuelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates similarity between users based on various factors:
 * - Booking history and preferences
 * - Vehicle ratings and reviews
 * - Demographic information
 * - Behavioral patterns
 */
@Component
@Slf4j
public class UserSimilarity {

    // Weight configuration for different similarity factors
    private static final double BOOKING_SIMILARITY_WEIGHT = 0.4;
    private static final double RATING_SIMILARITY_WEIGHT = 0.3;
    private static final double DEMOGRAPHIC_SIMILARITY_WEIGHT = 0.2;
    private static final double BEHAVIORAL_SIMILARITY_WEIGHT = 0.1;

    /**
     * Calculate overall similarity between two users
     */
    public double calculateOverallSimilarity(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return 0.0;
        }

        // Avoid comparing user with themselves
        if (user1.getId().equals(user2.getId())) {
            return 1.0;
        }

        double bookingSimilarity = calculateBookingSimilarity(user1, user2);
        double ratingSimilarity = calculateRatingSimilarity(user1, user2);
        double demographicSimilarity = calculateDemographicSimilarity(user1, user2);
        double behavioralSimilarity = calculateBehavioralSimilarity(user1, user2);

        double overallSimilarity = 
            (BOOKING_SIMILARITY_WEIGHT * bookingSimilarity) +
            (RATING_SIMILARITY_WEIGHT * ratingSimilarity) +
            (DEMOGRAPHIC_SIMILARITY_WEIGHT * demographicSimilarity) +
            (BEHAVIORAL_SIMILARITY_WEIGHT * behavioralSimilarity);

        log.debug("User similarity calculated: {} vs {} = {} (booking: {}, rating: {}, demographic: {}, behavioral: {})",
                 user1.getId(), user2.getId(), overallSimilarity, bookingSimilarity, 
                 ratingSimilarity, demographicSimilarity, behavioralSimilarity);

        return overallSimilarity;
    }

    /**
     * Calculate similarity based on booking history and preferences
     */
    public double calculateBookingSimilarity(User user1, User user2) {
        List<Booking> bookings1 = user1.getBookings();
        List<Booking> bookings2 = user2.getBookings();

        if (bookings1.isEmpty() && bookings2.isEmpty()) {
            return 0.5; // Neutral similarity if both have no bookings
        }
        if (bookings1.isEmpty() || bookings2.isEmpty()) {
            return 0.0; // No similarity if one has no bookings
        }

        // Extract vehicle types from bookings
        Set<VehicleType> types1 = bookings1.stream()
                .map(booking -> booking.getVehicle().getType())
                .collect(Collectors.toSet());
        Set<VehicleType> types2 = bookings2.stream()
                .map(booking -> booking.getVehicle().getType())
                .collect(Collectors.toSet());

        // Calculate Jaccard similarity for vehicle types
        double typeSimilarity = calculateJaccardSimilarity(types1, types2);

        // Calculate similarity based on booking frequency and patterns
        double patternSimilarity = calculateBookingPatternSimilarity(bookings1, bookings2);

        // Calculate price range similarity
        double priceSimilarity = calculatePriceRangeSimilarity(bookings1, bookings2);

        // Calculate duration similarity
        double durationSimilarity = calculateDurationSimilarity(bookings1, bookings2);

        return (typeSimilarity * 0.4) + (patternSimilarity * 0.3) + 
               (priceSimilarity * 0.2) + (durationSimilarity * 0.1);
    }

    /**
     * Calculate similarity based on rating patterns
     */
    public double calculateRatingSimilarity(User user1, User user2) {
        List<Review> reviews1 = user1.getReviews();
        List<Review> reviews2 = user2.getReviews();

        if (reviews1.isEmpty() && reviews2.isEmpty()) {
            return 0.5;
        }
        if (reviews1.isEmpty() || reviews2.isEmpty()) {
            return 0.0;
        }

        // Find common vehicles that both users have reviewed
        Map<Long, Integer> ratings1 = reviews1.stream()
                .collect(Collectors.toMap(
                    review -> review.getVehicle().getId(),
                    Review::getRating
                ));
        Map<Long, Integer> ratings2 = reviews2.stream()
                .collect(Collectors.toMap(
                    review -> review.getVehicle().getId(),
                    Review::getRating
                ));

        Set<Long> commonVehicles = new HashSet<>(ratings1.keySet());
        commonVehicles.retainAll(ratings2.keySet());

        if (commonVehicles.isEmpty()) {
            return 0.0;
        }

        // Calculate Pearson correlation for common ratings
        double correlation = calculatePearsonCorrelation(ratings1, ratings2, commonVehicles);

        // Normalize correlation to 0-1 range
        return Math.max(0.0, (correlation + 1) / 2);
    }

    /**
     * Calculate similarity based on demographic information
     */
    public double calculateDemographicSimilarity(User user1, User user2) {
        double similarity = 0.0;
        int factors = 0;

        // Age similarity (if we had age information)
        // For now, we'll use registration date as a proxy
        if (user1.getCreatedAt() != null && user2.getCreatedAt() != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(
                user1.getCreatedAt().toLocalDate(), 
                user2.getCreatedAt().toLocalDate()
            ));
            double timeSimilarity = Math.max(0, 1.0 - (daysDiff / 365.0)); // Normalize to 1 year
            similarity += timeSimilarity;
            factors++;
        }

        // Location similarity (simplified - in real implementation, use geolocation)
        if (user1.getBookings() != null && !user1.getBookings().isEmpty() &&
            user2.getBookings() != null && !user2.getBookings().isEmpty()) {
            
            String commonLocation1 = findMostCommonLocation(user1);
            String commonLocation2 = findMostCommonLocation(user2);
            
            if (commonLocation1 != null && commonLocation2 != null) {
                double locationSimilarity = commonLocation1.equalsIgnoreCase(commonLocation2) ? 1.0 : 0.0;
                similarity += locationSimilarity;
                factors++;
            }
        }

        return factors > 0 ? similarity / factors : 0.0;
    }

    /**
     * Calculate similarity based on behavioral patterns
     */
    public double calculateBehavioralSimilarity(User user1, User user2) {
        List<Booking> bookings1 = user1.getBookings();
        List<Booking> bookings2 = user2.getBookings();

        if (bookings1.isEmpty() || bookings2.isEmpty()) {
            return 0.0;
        }

        // Booking frequency similarity
        double frequencySimilarity = calculateFrequencySimilarity(bookings1, bookings2);

        // Seasonal pattern similarity
        double seasonalSimilarity = calculateSeasonalSimilarity(bookings1, bookings2);

        // Advance booking similarity
        double advanceBookingSimilarity = calculateAdvanceBookingSimilarity(bookings1, bookings2);

        return (frequencySimilarity * 0.4) + (seasonalSimilarity * 0.3) + (advanceBookingSimilarity * 0.3);
    }

    /**
     * Find the top N most similar users to a target user
     */
    public List<UserSimilarityScore> findSimilarUsers(User targetUser, List<User> allUsers, int topN) {
        return allUsers.stream()
                .filter(user -> !user.getId().equals(targetUser.getId()))
                .map(user -> new UserSimilarityScore(user, calculateOverallSimilarity(targetUser, user)))
                .sorted(Comparator.comparing(UserSimilarityScore::getSimilarityScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Find users with similar vehicle preferences
     */
    public List<UserSimilarityScore> findUsersWithSimilarVehiclePreferences(User targetUser, List<User> allUsers, int topN) {
        return allUsers.stream()
                .filter(user -> !user.getId().equals(targetUser.getId()))
                .map(user -> new UserSimilarityScore(user, calculateBookingSimilarity(targetUser, user)))
                .sorted(Comparator.comparing(UserSimilarityScore::getSimilarityScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Find users with similar rating patterns
     */
    public List<UserSimilarityScore> findUsersWithSimilarRatingPatterns(User targetUser, List<User> allUsers, int topN) {
        return allUsers.stream()
                .filter(user -> !user.getId().equals(targetUser.getId()))
                .map(user -> new UserSimilarityScore(user, calculateRatingSimilarity(targetUser, user)))
                .sorted(Comparator.comparing(UserSimilarityScore::getSimilarityScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity matrix for a group of users
     */
    public Map<Long, Map<Long, Double>> calculateSimilarityMatrix(List<User> users) {
        Map<Long, Map<Long, Double>> similarityMatrix = new HashMap<>();

        for (User user1 : users) {
            Map<Long, Double> userSimilarities = new HashMap<>();
            for (User user2 : users) {
                if (user1.getId().equals(user2.getId())) {
                    userSimilarities.put(user2.getId(), 1.0);
                } else {
                    double similarity = calculateOverallSimilarity(user1, user2);
                    userSimilarities.put(user2.getId(), similarity);
                }
            }
            similarityMatrix.put(user1.getId(), userSimilarities);
        }

        return similarityMatrix;
    }

    // Helper methods

    private double calculateJaccardSimilarity(Set<?> set1, Set<?> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }

        Set<Object> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Object> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private double calculateBookingPatternSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        // Compare booking frequencies
        double avgDuration1 = calculateAverageBookingDuration(bookings1);
        double avgDuration2 = calculateAverageBookingDuration(bookings2);

        double durationSimilarity = 1.0 - Math.abs(avgDuration1 - avgDuration2) / Math.max(avgDuration1, avgDuration2);

        // Compare booking intervals (if we have enough data)
        if (bookings1.size() > 1 && bookings2.size() > 1) {
            double intervalSimilarity = calculateBookingIntervalSimilarity(bookings1, bookings2);
            return (durationSimilarity * 0.6) + (intervalSimilarity * 0.4);
        }

        return durationSimilarity;
    }

    private double calculatePriceRangeSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        double avgPrice1 = calculateAverageBookingPrice(bookings1);
        double avgPrice2 = calculateAverageBookingPrice(bookings2);

        if (avgPrice1 == 0 && avgPrice2 == 0) {
            return 1.0;
        }
        if (avgPrice1 == 0 || avgPrice2 == 0) {
            return 0.0;
        }

        double priceRatio = Math.min(avgPrice1, avgPrice2) / Math.max(avgPrice1, avgPrice2);
        return Math.max(0.0, priceRatio);
    }

    private double calculateDurationSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        double avgDuration1 = calculateAverageBookingDuration(bookings1);
        double avgDuration2 = calculateAverageBookingDuration(bookings2);

        if (avgDuration1 == 0 && avgDuration2 == 0) {
            return 1.0;
        }
        if (avgDuration1 == 0 || avgDuration2 == 0) {
            return 0.0;
        }

        double durationRatio = Math.min(avgDuration1, avgDuration2) / Math.max(avgDuration1, avgDuration2);
        return Math.max(0.0, durationRatio);
    }

    private double calculatePearsonCorrelation(Map<Long, Integer> ratings1, Map<Long, Integer> ratings2, Set<Long> commonItems) {
        if (commonItems.size() < 2) {
            return 0.0;
        }

        double sum1 = 0.0, sum2 = 0.0;
        double sum1Sq = 0.0, sum2Sq = 0.0;
        double pSum = 0.0;

        for (Long itemId : commonItems) {
            int rating1 = ratings1.get(itemId);
            int rating2 = ratings2.get(itemId);

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

    private String findMostCommonLocation(User user) {
        return user.getBookings().stream()
                .collect(Collectors.groupingBy(
                    Booking::getPickupLocation,
                    Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private double calculateFrequencySimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        long daysSinceFirst1 = calculateDaysSinceFirstBooking(bookings1);
        long daysSinceFirst2 = calculateDaysSinceFirstBooking(bookings2);

        if (daysSinceFirst1 == 0 || daysSinceFirst2 == 0) {
            return 0.0;
        }

        double frequency1 = (double) bookings1.size() / daysSinceFirst1;
        double frequency2 = (double) bookings2.size() / daysSinceFirst2;

        return 1.0 - Math.abs(frequency1 - frequency2) / Math.max(frequency1, frequency2);
    }

    private double calculateSeasonalSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        Map<Integer, Long> monthlyBookings1 = groupBookingsByMonth(bookings1);
        Map<Integer, Long> monthlyBookings2 = groupBookingsByMonth(bookings2);

        return calculateCosineSimilarity(monthlyBookings1, monthlyBookings2);
    }

    private double calculateAdvanceBookingSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        double avgAdvance1 = calculateAverageAdvanceBooking(bookings1);
        double avgAdvance2 = calculateAverageAdvanceBooking(bookings2);

        if (avgAdvance1 == 0 && avgAdvance2 == 0) {
            return 1.0;
        }
        if (avgAdvance1 == 0 || avgAdvance2 == 0) {
            return 0.0;
        }

        double advanceRatio = Math.min(avgAdvance1, avgAdvance2) / Math.max(avgAdvance1, avgAdvance2);
        return Math.max(0.0, advanceRatio);
    }

    private double calculateAverageBookingDuration(List<Booking> bookings) {
        return bookings.stream()
                .mapToLong(booking -> ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate()))
                .average()
                .orElse(0.0);
    }

    private double calculateAverageBookingPrice(List<Booking> bookings) {
        return bookings.stream()
                .mapToDouble(booking -> booking.getTotalPrice().doubleValue())
                .average()
                .orElse(0.0);
    }

    private double calculateBookingIntervalSimilarity(List<Booking> bookings1, List<Booking> bookings2) {
        // Simplified implementation - compare average time between bookings
        double avgInterval1 = calculateAverageBookingInterval(bookings1);
        double avgInterval2 = calculateAverageBookingInterval(bookings2);

        if (avgInterval1 == 0 && avgInterval2 == 0) {
            return 1.0;
        }
        if (avgInterval1 == 0 || avgInterval2 == 0) {
            return 0.0;
        }

        double intervalRatio = Math.min(avgInterval1, avgInterval2) / Math.max(avgInterval1, avgInterval2);
        return Math.max(0.0, intervalRatio);
    }

    private long calculateDaysSinceFirstBooking(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return 0;
        }
        LocalDate firstBooking = bookings.stream()
                .map(Booking::getCreatedAt)
                .min(LocalDate::compareTo)
                .get()
                .toLocalDate();
        return ChronoUnit.DAYS.between(firstBooking, LocalDate.now());
    }

    private Map<Integer, Long> groupBookingsByMonth(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(
                    booking -> booking.getStartDate().getMonthValue(),
                    Collectors.counting()
                ));
    }

    private double calculateCosineSimilarity(Map<Integer, Long> vec1, Map<Integer, Long> vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        Set<Integer> allMonths = new HashSet<>();
        allMonths.addAll(vec1.keySet());
        allMonths.addAll(vec2.keySet());

        for (Integer month : allMonths) {
            long val1 = vec1.getOrDefault(month, 0L);
            long val2 = vec2.getOrDefault(month, 0L);

            dotProduct += val1 * val2;
            norm1 += Math.pow(val1, 2);
            norm2 += Math.pow(val2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double calculateAverageAdvanceBooking(List<Booking> bookings) {
        return bookings.stream()
                .mapToLong(booking -> ChronoUnit.DAYS.between(
                    booking.getCreatedAt().toLocalDate(), 
                    booking.getStartDate()
                ))
                .average()
                .orElse(0.0);
    }

    private double calculateAverageBookingInterval(List<Booking> bookings) {
        if (bookings.size() < 2) {
            return 0.0;
        }

        List<LocalDate> sortedDates = bookings.stream()
                .map(booking -> booking.getCreatedAt().toLocalDate())
                .sorted()
                .collect(Collectors.toList());

        double totalInterval = 0.0;
        for (int i = 1; i < sortedDates.size(); i++) {
            totalInterval += ChronoUnit.DAYS.between(sortedDates.get(i - 1), sortedDates.get(i));
        }

        return totalInterval / (sortedDates.size() - 1);
    }

    /**
     * Inner class to represent user similarity scores
     */
    public static class UserSimilarityScore {
        private final User user;
        private final double similarityScore;

        public UserSimilarityScore(User user, double similarityScore) {
            this.user = user;
            this.similarityScore = similarityScore;
        }

        public User getUser() {
            return user;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        @Override
        public String toString() {
            return String.format("UserSimilarityScore{user=%d, similarity=%.3f}", 
                               user.getId(), similarityScore);
        }
    }

    /**
     * Configuration class for similarity calculation weights
     */
    @Data
    @Builder
    public static class SimilarityWeights {
        @Builder.Default
        private double bookingSimilarityWeight = 0.4;
        
        @Builder.Default
        private double ratingSimilarityWeight = 0.3;
        
        @Builder.Default
        private double demographicSimilarityWeight = 0.2;
        
        @Builder.Default
        private double behavioralSimilarityWeight = 0.1;

        public void validate() {
            double total = bookingSimilarityWeight + ratingSimilarityWeight + 
                          demographicSimilarityWeight + behavioralSimilarityWeight;
            if (Math.abs(total - 1.0) > 0.001) {
                throw new IllegalArgumentException("Similarity weights must sum to 1.0");
            }
        }
    }
}