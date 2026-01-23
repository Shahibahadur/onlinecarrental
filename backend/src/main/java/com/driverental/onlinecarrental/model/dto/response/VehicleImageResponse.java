package com.driverental.onlinecarrental.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImageResponse {
    private Long id;
    private Long vehicleId;
    private String imageName;
    private String imageUrl; // Full path for frontend
    private String category;
    private Integer displayOrder;
    private String altText;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CategorizedVehicleImagesResponse {
    private Long vehicleId;
    private String mainImage;
    
    @Builder.Default
    private List<VehicleImageResponse> exteriorImages = new ArrayList<>();
    
    @Builder.Default
    private List<VehicleImageResponse> interiorImages = new ArrayList<>();
    
    @Builder.Default
    private List<VehicleImageResponse> featureImages = new ArrayList<>();
    
    @Builder.Default
    private List<VehicleImageResponse> safetyImages = new ArrayList<>();
    
    @Builder.Default
    private List<VehicleImageResponse> amenityImages = new ArrayList<>();
    
    @Builder.Default
    private List<VehicleImageResponse> performanceImages = new ArrayList<>();
    
    @Builder.Default
    private Map<String, List<VehicleImageResponse>> imagesByCategory = new java.util.HashMap<>();
}
