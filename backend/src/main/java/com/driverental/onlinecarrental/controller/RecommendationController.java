package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
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
@Tag(name = "Recommendation", description = "Vehicle recommendation APIs")
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
        recommendations.forEach(vehicle -> 
            recommendationService.trackUserInteraction(userId, vehicle.getId(), "IMPRESSION"));
        
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/hybrid")
    @Operation(summary = "Get comprehensive hybrid recommendations")
    public ResponseEntity<RecommendationResponse> getHybridRecommendations(Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        
        RecommendationResponse response = recommendationService.getHybridRecommendations(userId);
        
        // Track impressions for all recommended vehicles
        response.getRecommendations().forEach(vehicle -> 
            recommendationService.trackUserInteraction(userId, vehicle.getId(), "IMPRESSION"));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular vehicles")
    public ResponseEntity<List<VehicleResponse>> getPopularVehicles() {
        var popularVehicles = recommendationService.getPopularVehicles()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(popularVehicles);
    }
    
    @GetMapping("/similar/{vehicleId}")
    @Operation(summary = "Get vehicles similar to a specific vehicle")
    public ResponseEntity<List<VehicleResponse>> getSimilarVehicles(@PathVariable Long vehicleId) {
        var similarVehicles = recommendationService.getSimilarVehicles(vehicleId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(similarVehicles);
    }
    
    @GetMapping("/trending")
    @Operation(summary = "Get currently trending vehicles")
    public ResponseEntity<List<VehicleResponse>> getTrendingVehicles() {
        var trendingVehicles = recommendationService.getTrendingVehicles()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(trendingVehicles);
    }
    
    @GetMapping("/explanations/{vehicleId}")
    @Operation(summary = "Get explanation for why a vehicle was recommended")
    public ResponseEntity<Map<String, Object>> getRecommendationExplanation(
            Authentication authentication,
            @PathVariable Long vehicleId) {
        Long userId = extractUserIdFromAuth(authentication);
        
        Map<String, Object> explanations = recommendationService.getRecommendationExplanations(userId, vehicleId);
        return ResponseEntity.ok(explanations);
    }
    
    @PostMapping("/interaction/{vehicleId}")
    @Operation(summary = "Track user interaction with a recommended vehicle")
    public ResponseEntity<Void> trackInteraction(
            Authentication authentication,
            @PathVariable Long vehicleId,
            @RequestParam String interactionType) {
        Long userId = extractUserIdFromAuth(authentication);
        
        recommendationService.trackUserInteraction(userId, vehicleId, interactionType);
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
    
    private VehicleResponse convertToResponse(com.driverental.onlinecarrental.model.entity.Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .type(vehicle.getType())
                .fuelType(vehicle.getFuelType())
                .transmission(vehicle.getTransmission())
                .seats(vehicle.getSeats())
                .luggageCapacity(vehicle.getLuggageCapacity())
                .features(vehicle.getFeatures())
                .basePrice(vehicle.getBasePrice())
                .dailyPrice(vehicle.getDailyPrice())
                .location(vehicle.getLocation())
                .imageUrl(vehicle.getImageUrl())
                .isAvailable(vehicle.getIsAvailable())
                .rating(vehicle.getRating())
                .reviewCount(vehicle.getReviewCount())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
    
    private Long extractUserIdFromAuth(Authentication authentication) {
        // Implementation to extract user ID from authentication
        // This would typically come from JWT token
        return 1L; // Placeholder - in real implementation, extract from SecurityContext
    }
}