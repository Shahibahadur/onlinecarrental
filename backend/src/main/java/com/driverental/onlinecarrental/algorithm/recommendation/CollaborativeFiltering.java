package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.model.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFiltering {

    private final UserSimilarity userSimilarity;

    /**
     * User-based collaborative filtering recommendations
     */
    public List<Car> userBasedRecommendations(User targetUser, List<User> allUsers,
            List<Car> allCars, int topK) {
        log.info("Generating user-based collaborative filtering recommendations for user: {}", targetUser.getId());

        // Find similar users
        List<UserSimilarity.UserSimilarityScore> similarUsers = userSimilarity.findSimilarUsers(targetUser, allUsers,
                topK);

        if (similarUsers.isEmpty()) {
            log.info("No similar users found for user: {}", targetUser.getId());
            return Collections.emptyList();
        }

        // Get cars booked by similar users but not by target user
        Set<Car> targetUserCars = getBookedCars(targetUser);
        Map<Car, Double> carScores = new HashMap<>();

        for (UserSimilarity.UserSimilarityScore similarUserScore : similarUsers) {
            User similarUser = similarUserScore.getUser();
            double similarity = similarUserScore.getSimilarityScore();

            Set<Car> similarUserCars = getBookedCars(similarUser);

            for (Car car : similarUserCars) {
                if (!targetUserCars.contains(car) && car.getIsAvailable()) {
                    // Score car based on user similarity and car popularity
                    double score = similarity * calculateCarPopularity(car, allUsers);
                    carScores.merge(car, score, Double::sum);
                }
            }
        }

        // Return top scored cars
        return carScores.entrySet().stream()
                .sorted(Map.Entry.<Car, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Item-based collaborative filtering recommendations
     */
    public List<Car> itemBasedRecommendations(User user, List<Car> allCars, int topK) {
        log.info("Generating item-based collaborative filtering recommendations for user: {}", user.getId());

        Set<Car> userCars = getBookedCars(user);

        if (userCars.isEmpty()) {
            log.info("User {} has no booking history for item-based recommendations", user.getId());
            return Collections.emptyList();
        }

        Map<Car, Double> carScores = new HashMap<>();

        for (Car userCar : userCars) {
            List<Car> similarCars = findSimilarCars(userCar, allCars, topK);

            for (Car similarCar : similarCars) {
                if (!userCars.contains(similarCar) && similarCar.getIsAvailable()) {
                    double similarity = calculateCarSimilarity(userCar, similarCar);
                    carScores.merge(similarCar, similarity, Double::sum);
                }
            }
        }

        return carScores.entrySet().stream()
                .sorted(Map.Entry.<Car, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Find similar cars based on various attributes
     */
    private List<Car> findSimilarCars(Car targetCar, List<Car> allCars, int topK) {
        return allCars.stream()
                .filter(car -> !car.getId().equals(targetCar.getId()))
                .filter(Car::getIsAvailable)
                .sorted((v1, v2) -> Double.compare(
                        calculateCarSimilarity(targetCar, v2),
                        calculateCarSimilarity(targetCar, v1)))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between two cars
     */
    private double calculateCarSimilarity(Car v1, Car v2) {
        double similarity = 0.0;

        // Type similarity
        if (v1.getType().equals(v2.getType())) {
            similarity += 0.3;
        }

        // Fuel type similarity
        if (v1.getFuelType().equals(v2.getFuelType())) {
            similarity += 0.2;
        }

        // Price similarity (within 20%)
        double priceDiff = Math.abs(v1.getDailyPrice().doubleValue() - v2.getDailyPrice().doubleValue());
        double avgPrice = (v1.getDailyPrice().doubleValue() + v2.getDailyPrice().doubleValue()) / 2;
        if (priceDiff / avgPrice <= 0.2) {
            similarity += 0.2;
        }

        // Feature similarity
        Set<String> commonFeatures = new HashSet<>(v1.getFeatures());
        commonFeatures.retainAll(v2.getFeatures());
        double featureSimilarity = (double) commonFeatures.size() /
                Math.max(v1.getFeatures().size(), v2.getFeatures().size());
        similarity += featureSimilarity * 0.3;

        return similarity;
    }

    /**
     * Calculate car popularity based on booking frequency and ratings
     */
    private double calculateCarPopularity(Car car, List<User> allUsers) {
        long bookingCount = allUsers.stream()
                .map(User::getBookings)
                .flatMap(List::stream)
                .filter(booking -> booking.getCar().getId().equals(car.getId()))
                .count();

        double ratingScore = car.getRating() / 5.0; // Normalize to 0-1

        return (bookingCount * 0.6) + (ratingScore * 0.4);
    }

    /**
     * Get all cars booked by a user
     */
    private Set<Car> getBookedCars(User user) {
        return user.getBookings().stream()
                .map(Booking::getCar)
                .collect(Collectors.toSet());
    }

    /**
     * Generate explanations for recommendations
     */
    public Map<String, Object> generateRecommendationExplanations(User targetUser, Car recommendedCar,
            List<User> allUsers, int topK) {
        Map<String, Object> explanations = new HashMap<>();

        // Find similar users who booked this car
        List<UserSimilarity.UserSimilarityScore> similarUsers = userSimilarity.findSimilarUsers(targetUser, allUsers,
                topK);

        List<String> similarUserNames = similarUsers.stream()
                .filter(score -> hasBookedCar(score.getUser(), recommendedCar))
                .map(score -> score.getUser().getFirstName() + " " + score.getUser().getLastName())
                .limit(3)
                .collect(Collectors.toList());

        if (!similarUserNames.isEmpty()) {
            explanations.put("similarUsers", similarUserNames);
        }

        // Car popularity
        double popularity = calculateCarPopularity(recommendedCar, allUsers);
        explanations.put("popularityScore", popularity);

        // Feature match with user preferences
        List<String> userPreferredFeatures = inferPreferredFeatures(targetUser);
        List<String> matchingFeatures = recommendedCar.getFeatures().stream()
                .filter(userPreferredFeatures::contains)
                .collect(Collectors.toList());

        if (!matchingFeatures.isEmpty()) {
            explanations.put("matchingFeatures", matchingFeatures);
        }

        return explanations;
    }

    private boolean hasBookedCar(User user, Car car) {
        return user.getBookings().stream()
                .anyMatch(booking -> booking.getCar().getId().equals(car.getId()));
    }

    private List<String> inferPreferredFeatures(User user) {
        return user.getBookings().stream()
                .map(Booking::getCar)
                .flatMap(car -> car.getFeatures().stream())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }
}