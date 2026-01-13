package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.security.UserPrincipal;
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
    public ResponseEntity<List<VehicleResponse>> getPersonalizedRecommendations(Authentication authentication) {
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
    public ResponseEntity<List<VehicleResponse>> getPopularVehicles() {
        var popularVehicles = recommendationService.getPopularVehicles()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(popularVehicles);
    }

    @GetMapping("/similar/{vehicleId}")
    @Operation(summary = "Get cars similar to a specific car")
    public ResponseEntity<List<VehicleResponse>> getSimilarVehicles(@PathVariable Long vehicleId) {
        var similarVehicles = recommendationService.getSimilarVehicles(vehicleId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(similarVehicles);
    }

    @GetMapping("/trending")
    @Operation(summary = "Get currently trending cars")
    public ResponseEntity<List<VehicleResponse>> getTrendingVehicles() {
        var trendingVehicles = recommendationService.getTrendingVehicles()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(trendingVehicles);
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

    private VehicleResponse convertToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .type(vehicle.getType())
                .dailyPrice(vehicle.getDailyPrice())
                .imageUrl(vehicle.getImageUrl())
                .isAvailable(vehicle.getIsAvailable())
                .rating(vehicle.getRating())
                .reviewCount(vehicle.getReviewCount())
                .features(vehicle.getFeatures())
                .location(vehicle.getLocation())
                .transmission(vehicle.getTransmission())
                .fuelType(vehicle.getFuelType())
                .engineCapacity(vehicle.getEngineCapacity())
                .seats(vehicle.getSeats())
                .description(vehicle.getDescription())
                .licensePlate(vehicle.getLicensePlate())
                // .mileage(vehicle.getMileage()) // Add if available in Vehicle entity
                .build();
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new com.driverental.onlinecarrental.model.exception.BusinessException("User not authenticated");
    }
}