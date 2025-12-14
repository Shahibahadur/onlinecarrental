package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Car;
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

    public List<Car> getHybridRecommendations(User user, List<User> allUsers,
            List<Car> allCars, int topN) {
        // Get recommendations from different algorithms
        List<Car> userBased = collaborativeFiltering.userBasedRecommendations(user, allUsers, allCars, 5);
        List<Car> itemBased = collaborativeFiltering.itemBasedRecommendations(user, allCars, 10);

        // Combine and rank recommendations
        Map<Car, Double> combinedScores = new HashMap<>();

        // Score user-based recommendations
        for (int i = 0; i < userBased.size(); i++) {
            Car car = userBased.get(i);
            double score = 1.0 - (i * 0.1); // Higher score for top positions
            combinedScores.merge(car, score * 0.4, Double::sum);
        }

        // Score item-based recommendations
        for (int i = 0; i < itemBased.size(); i++) {
            Car car = itemBased.get(i);
            double score = 1.0 - (i * 0.05); // Lower decay for item-based
            combinedScores.merge(car, score * 0.3, Double::sum);
        }

        // Add popularity/diversity factor
        for (Car car : allCars) {
            if (!combinedScores.containsKey(car)) {
                double popularityScore = calculatePopularityScore(car);
                combinedScores.put(car, popularityScore * 0.3);
            }
        }

        // Return top N recommendations
        return combinedScores.entrySet().stream()
                .sorted(Map.Entry.<Car, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculatePopularityScore(Car car) {
        double ratingScore = car.getRating() / 5.0;
        double reviewScore = Math.min(1.0, car.getReviewCount() / 50.0);

        return 0.6 * ratingScore + 0.4 * reviewScore;
    }
}