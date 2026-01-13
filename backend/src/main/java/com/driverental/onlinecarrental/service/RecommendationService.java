package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;

import java.util.List;
import java.util.Map;

public interface RecommendationService {

    /**
     * Get personalized car recommendations for a user
     */
    List<Vehicle> getRecommendationsForUser(Long userId);

    /**
     * Get popular cars based on ratings and reviews
     */
    List<Vehicle> getPopularVehicles();

    /**
     * Get similar cars based on a target car
     */
    List<Vehicle> getSimilarVehicles(Long vehicleId);

    /**
     * Get recommendations based on user's search history
     */
    List<Vehicle> getSearchBasedRecommendations(Long userId);

    /**
     * Get recommendations based on car features
     */
    List<Vehicle> getFeatureBasedRecommendations(Long userId, List<String> preferredFeatures);

    /**
     * Get location-based recommendations
     */
    List<Vehicle> getLocationBasedRecommendations(Long userId, String location);

    /**
     * Get price-based recommendations within a budget
     */
    List<Vehicle> getBudgetBasedRecommendations(Long userId, Double maxDailyPrice);

    /**
     * Get hybrid recommendations combining multiple approaches
     */
    RecommendationResponse getHybridRecommendations(Long userId);

    /**
     * Get recommendation explanations for transparency
     */
    Map<String, Object> getRecommendationExplanations(Long userId, Long vehicleId);

    /**
     * Refresh recommendation model (for admin purposes)
     */
    void refreshRecommendationModel();

    /**
     * Get recommendation performance metrics (for admin purposes)
     */
    Map<String, Double> getRecommendationMetrics();

    /**
     * Track user interaction for recommendation improvement
     */
    void trackUserInteraction(Long userId, Long vehicleId, String interactionType);

    /**
     * Get trending cars (recently popular)
     */
    List<Vehicle> getTrendingVehicles();

    /**
     * Get recommendations for new users (cold start problem)
     */
    List<Vehicle> getColdStartRecommendations();

    /**
     * Get diverse recommendations to avoid filter bubbles
     */
    List<Vehicle> getDiverseRecommendations(Long userId, Integer diversityFactor);

    /**
     * Get seasonal recommendations based on current time
     */
    List<Vehicle> getSeasonalRecommendations();

    /**
     * Check if user has sufficient data for personalized recommendations
     */
    Boolean hasSufficientDataForPersonalization(Long userId);
}