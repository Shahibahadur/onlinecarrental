package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "Vehicle recommendation APIs")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/user")
    @Operation(summary = "Get personalized recommendations for authenticated user")
    public ResponseEntity<List<VehicleResponse>> getUserRecommendations(Authentication authentication) {
        // In real implementation, extract user ID from authentication
        Long userId = 1L; // Placeholder
        
        var recommendations = recommendationService.getRecommendationsForUser(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(recommendations);
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
    
    private VehicleResponse convertToResponse(com.driverental.onlinecarrental.model.entity.Vehicle vehicle) {
        // Implementation to convert entity to response DTO
        return new VehicleResponse(); // Placeholder
    }
}