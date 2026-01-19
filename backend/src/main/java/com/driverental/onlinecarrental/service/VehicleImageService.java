package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.VehicleImageRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleImageResponse;
import com.driverental.onlinecarrental.model.enums.ImageCategory;

import java.util.List;

public interface VehicleImageService {
    /**
     * Get all images for a vehicle
     */
    List<VehicleImageResponse> getVehicleImages(Long vehicleId);

    /**
     * Get images for a vehicle by category
     */
    List<VehicleImageResponse> getVehicleImagesByCategory(Long vehicleId, ImageCategory category);

    /**
     * Upload and store an image for a vehicle
     */
    VehicleImageResponse addVehicleImage(VehicleImageRequest request);

    /**
     * Update image metadata
     */
    VehicleImageResponse updateVehicleImage(Long imageId, VehicleImageRequest request);

    /**
     * Delete an image
     */
    void deleteVehicleImage(Long imageId);

    /**
     * Delete all images in a category for a vehicle
     */
    void deleteVehicleImagesByCategory(Long vehicleId, ImageCategory category);

    /**
     * Set main image for a vehicle
     */
    void setMainImage(Long vehicleId, Long imageId);

    /**
     * Reorder images within a category
     */
    void reorderImages(Long vehicleId, ImageCategory category, List<Long> imageIds);

    /**
     * Get main image for a vehicle
     */
    VehicleImageResponse getMainImage(Long vehicleId);

    /**
     * Count images for a vehicle
     */
    long countVehicleImages(Long vehicleId);
}
