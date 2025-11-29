package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class HybridRecommender {
    
    private final CollaborativeFiltering collaborativeFiltering;
    private final MatrixFactorization matrixFactorization;
    
    public List<Vehicle> getHybridRecommendations(User user, List<User> allUsers, 
                                                 List<Vehicle> allVehicles, int topN) {
        // Get recommendations from different algorithms
        List<Vehicle> userBased = collaborativeFiltering.userBasedRecommendations(user, allUsers, allVehicles, 5);
        List<Vehicle> itemBased = collaborativeFiltering.itemBasedRecommendations(user, allVehicles, 10);
        
        // Combine and rank recommendations
        Map<Vehicle, Double> combinedScores = new HashMap<>();
        
        // Score user-based recommendations
        for (int i = 0; i < userBased.size(); i++) {
            Vehicle vehicle = userBased.get(i);
            double score = 1.0 - (i * 0.1); // Higher score for top positions
            combinedScores.merge(vehicle, score * 0.4, Double::sum);
        }
        
        // Score item-based recommendations
        for (int i = 0; i < itemBased.size(); i++) {
            Vehicle vehicle = itemBased.get(i);
            double score = 1.0 - (i * 0.05); // Lower decay for item-based
            combinedScores.merge(vehicle, score * 0.3, Double::sum);
        }
        
        // Add popularity/diversity factor
        for (Vehicle vehicle : allVehicles) {
            if (!combinedScores.containsKey(vehicle)) {
                double popularityScore = calculatePopularityScore(vehicle);
                combinedScores.put(vehicle, popularityScore * 0.3);
            }
        }
        
        // Return top N recommendations
        return combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Vehicle, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    private double calculatePopularityScore(Vehicle vehicle) {
        double ratingScore = vehicle.getRating() / 5.0;
        double reviewScore = Math.min(1.0, vehicle.getReviewCount() / 50.0);
        
        return 0.6 * ratingScore + 0.4 * reviewScore;
    }
}