package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
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
    public List<Vehicle> userBasedRecommendations(User targetUser, List<User> allUsers, 
                                                 List<Vehicle> allVehicles, int topK) {
        log.info("Generating user-based collaborative filtering recommendations for user: {}", targetUser.getId());
        
        // Find similar users
        List<UserSimilarity.UserSimilarityScore> similarUsers = 
            userSimilarity.findSimilarUsers(targetUser, allUsers, topK);
        
        if (similarUsers.isEmpty()) {
            log.info("No similar users found for user: {}", targetUser.getId());
            return Collections.emptyList();
        }
        
        // Get vehicles booked by similar users but not by target user
        Set<Vehicle> targetUserVehicles = getBookedVehicles(targetUser);
        Map<Vehicle, Double> vehicleScores = new HashMap<>();
        
        for (UserSimilarity.UserSimilarityScore similarUserScore : similarUsers) {
            User similarUser = similarUserScore.getUser();
            double similarity = similarUserScore.getSimilarityScore();
            
            Set<Vehicle> similarUserVehicles = getBookedVehicles(similarUser);
            
            for (Vehicle vehicle : similarUserVehicles) {
                if (!targetUserVehicles.contains(vehicle) && vehicle.getIsAvailable()) {
                    // Score vehicle based on user similarity and vehicle popularity
                    double score = similarity * calculateVehiclePopularity(vehicle, allUsers);
                    vehicleScores.merge(vehicle, score, Double::sum);
                }
            }
        }
        
        // Return top scored vehicles
        return vehicleScores.entrySet().stream()
                .sorted(Map.Entry.<Vehicle, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Item-based collaborative filtering recommendations
     */
    public List<Vehicle> itemBasedRecommendations(User user, List<Vehicle> allVehicles, int topK) {
        log.info("Generating item-based collaborative filtering recommendations for user: {}", user.getId());
        
        Set<Vehicle> userVehicles = getBookedVehicles(user);
        
        if (userVehicles.isEmpty()) {
            log.info("User {} has no booking history for item-based recommendations", user.getId());
            return Collections.emptyList();
        }
        
        Map<Vehicle, Double> vehicleScores = new HashMap<>();
        
        for (Vehicle userVehicle : userVehicles) {
            List<Vehicle> similarVehicles = findSimilarVehicles(userVehicle, allVehicles, topK);
            
            for (Vehicle similarVehicle : similarVehicles) {
                if (!userVehicles.contains(similarVehicle) && similarVehicle.getIsAvailable()) {
                    double similarity = calculateVehicleSimilarity(userVehicle, similarVehicle);
                    vehicleScores.merge(similarVehicle, similarity, Double::sum);
                }
            }
        }
        
        return vehicleScores.entrySet().stream()
                .sorted(Map.Entry.<Vehicle, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Find similar vehicles based on various attributes
     */
    private List<Vehicle> findSimilarVehicles(Vehicle targetVehicle, List<Vehicle> allVehicles, int topK) {
        return allVehicles.stream()
                .filter(vehicle -> !vehicle.getId().equals(targetVehicle.getId()))
                .filter(Vehicle::getIsAvailable)
                .sorted((v1, v2) -> Double.compare(
                    calculateVehicleSimilarity(targetVehicle, v2),
                    calculateVehicleSimilarity(targetVehicle, v1)
                ))
                .limit(topK)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate similarity between two vehicles
     */
    private double calculateVehicleSimilarity(Vehicle v1, Vehicle v2) {
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
     * Calculate vehicle popularity based on booking frequency and ratings
     */
    private double calculateVehiclePopularity(Vehicle vehicle, List<User> allUsers) {
        long bookingCount = allUsers.stream()
                .map(User::getBookings)
                .flatMap(List::stream)
                .filter(booking -> booking.getVehicle().getId().equals(vehicle.getId()))
                .count();
        
        double ratingScore = vehicle.getRating() / 5.0; // Normalize to 0-1
        
        return (bookingCount * 0.6) + (ratingScore * 0.4);
    }
    
    /**
     * Get all vehicles booked by a user
     */
    private Set<Vehicle> getBookedVehicles(User user) {
        return user.getBookings().stream()
                .map(Booking::getVehicle)
                .collect(Collectors.toSet());
    }
    
    /**
     * Generate explanations for recommendations
     */
    public Map<String, Object> generateRecommendationExplanations(User targetUser, Vehicle recommendedVehicle, 
                                                                 List<User> allUsers, int topK) {
        Map<String, Object> explanations = new HashMap<>();
        
        // Find similar users who booked this vehicle
        List<UserSimilarity.UserSimilarityScore> similarUsers = 
            userSimilarity.findSimilarUsers(targetUser, allUsers, topK);
        
        List<String> similarUserNames = similarUsers.stream()
                .filter(score -> hasBookedVehicle(score.getUser(), recommendedVehicle))
                .map(score -> score.getUser().getFirstName() + " " + score.getUser().getLastName())
                .limit(3)
                .collect(Collectors.toList());
        
        if (!similarUserNames.isEmpty()) {
            explanations.put("similarUsers", similarUserNames);
        }
        
        // Vehicle popularity
        double popularity = calculateVehiclePopularity(recommendedVehicle, allUsers);
        explanations.put("popularityScore", popularity);
        
        // Feature match with user preferences
        List<String> userPreferredFeatures = inferPreferredFeatures(targetUser);
        List<String> matchingFeatures = recommendedVehicle.getFeatures().stream()
                .filter(userPreferredFeatures::contains)
                .collect(Collectors.toList());
        
        if (!matchingFeatures.isEmpty()) {
            explanations.put("matchingFeatures", matchingFeatures);
        }
        
        return explanations;
    }
    
    private boolean hasBookedVehicle(User user, Vehicle vehicle) {
        return user.getBookings().stream()
                .anyMatch(booking -> booking.getVehicle().getId().equals(vehicle.getId()));
    }
    
    private List<String> inferPreferredFeatures(User user) {
        return user.getBookings().stream()
                .map(Booking::getVehicle)
                .flatMap(vehicle -> vehicle.getFeatures().stream())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }
}