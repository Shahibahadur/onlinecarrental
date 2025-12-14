package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import com.driverental.onlinecarrental.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "Car recommendation APIs")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personalized")
    @Operation(summary = "Get personalized recommendations for authenticated user")
    public ResponseEntity<List<CarResponse>> getPersonalizedRecommendations(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);

        var recommendations = recommendationService.getRecommendationsForUser(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Track this recommendation impression
        recommendations.forEach(car -> recommendationService.trackUserInteraction(userId, car.getId(), "IMPRESSION"));

        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/hybrid")
    @Operation(summary = "Get comprehensive hybrid recommendations")
    public ResponseEntity<RecommendationResponse> getHybridRecommendations(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);

        RecommendationResponse response = recommendationService.getHybridRecommendations(userId);

        // Track impressions for all recommended cars
        response.getRecommendations()
                .forEach(car -> recommendationService.trackUserInteraction(userId, car.getId(), "IMPRESSION"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular cars")
    public ResponseEntity<List<CarResponse>> getPopularCars() {
        var popularCars = recommendationService.getPopularCars()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(popularCars);
    }

    @GetMapping("/similar/{carId}")
    @Operation(summary = "Get cars similar to a specific car")
    public ResponseEntity<List<CarResponse>> getSimilarCars(@PathVariable Long carId) {
        var similarCars = recommendationService.getSimilarCars(carId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(similarCars);
    }

    @GetMapping("/trending")
    @Operation(summary = "Get currently trending cars")
    public ResponseEntity<List<CarResponse>> getTrendingCars() {
        var trendingCars = recommendationService.getTrendingCars()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(trendingCars);
    }

    @GetMapping("/explanations/{carId}")
    @Operation(summary = "Get explanation for why a car was recommended")
    public ResponseEntity<Map<String, Object>> getRecommendationExplanation(
            Authentication authentication,
            @PathVariable Long carId) {
        Long userId = extractUserIdFromAuth(authentication);

        Map<String, Object> explanations = recommendationService.getRecommendationExplanations(userId, carId);
        return ResponseEntity.ok(explanations);
    }

    @PostMapping("/interaction/{carId}")
    @Operation(summary = "Track user interaction with a recommended car")
    public ResponseEntity<Void> trackInteraction(
            Authentication authentication,
            @PathVariable Long carId,
            @RequestParam String interactionType) {
        Long userId = extractUserIdFromAuth(authentication);

        recommendationService.trackUserInteraction(userId, carId, interactionType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get recommendation system performance metrics (Admin)")
    public ResponseEntity<Map<String, Double>> getRecommendationMetrics() {
        Map<String, Double> metrics = recommendationService.getRecommendationMetrics();
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh recommendation models (Admin)")
    public ResponseEntity<Void> refreshRecommendationModels() {
        recommendationService.refreshRecommendationModel();
        return ResponseEntity.ok().build();
    }

    private CarResponse convertToResponse(com.driverental.onlinecarrental.model.entity.Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .year(car.getYear())
                .type(car.getType())
                .fuelType(car.getFuelType())
                .transmission(car.getTransmission())
                .seats(car.getSeats())
                .luggageCapacity(car.getLuggageCapacity())
                .features(car.getFeatures())
                .basePrice(car.getBasePrice())
                .dailyPrice(car.getDailyPrice())
                .location(car.getLocation())
                .imageUrl(car.getImageUrl())
                .isAvailable(car.getIsAvailable())
                .rating(car.getRating())
                .reviewCount(car.getReviewCount())
                .createdAt(car.getCreatedAt())
                .build();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        // Implementation to extract user ID from authentication
        // This would typically come from JWT token
        return 1L; // Placeholder - in real implementation, extract from SecurityContext
    }
}