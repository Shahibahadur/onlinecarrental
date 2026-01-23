package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.VehicleImageRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleImageResponse;
import com.driverental.onlinecarrental.service.VehicleImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-images")
@Tag(name = "Vehicle Images", description = "Categorized vehicle image management")
@RequiredArgsConstructor
public class VehicleImageController {

    private final VehicleImageService vehicleImageService;

    /**
     * Get all images for a vehicle
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get all images for a vehicle")
    public ResponseEntity<List<VehicleImageResponse>> getVehicleImages(@PathVariable Long vehicleId) {
        List<VehicleImageResponse> images = vehicleImageService.getVehicleImages(vehicleId);
        return ResponseEntity.ok(images);
    }

    /**
     * Get images for a vehicle by category
     */
    @GetMapping("/vehicle/{vehicleId}/category/{category}")
    @Operation(summary = "Get images for a vehicle by category")
    public ResponseEntity<List<VehicleImageResponse>> getVehicleImagesByCategory(
            @PathVariable Long vehicleId,
            @PathVariable String category) {
        List<VehicleImageResponse> images = vehicleImageService.getVehicleImagesByCategory(vehicleId, category);
        return ResponseEntity.ok(images);
    }

    /**
     * Get main image for a vehicle
     */
    @GetMapping("/vehicle/{vehicleId}/main")
    @Operation(summary = "Get main image for a vehicle")
    public ResponseEntity<VehicleImageResponse> getMainImage(@PathVariable Long vehicleId) {
        VehicleImageResponse mainImage = vehicleImageService.getMainImage(vehicleId);
        if (mainImage != null) {
            return ResponseEntity.ok(mainImage);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Add a new image for a vehicle
     */
    @PostMapping
    @Operation(summary = "Add a new image for a vehicle (Admin only)")
    public ResponseEntity<VehicleImageResponse> addVehicleImage(@RequestBody VehicleImageRequest request) {
        VehicleImageResponse image = vehicleImageService.addVehicleImage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }

    /**
     * Update image metadata
     */
    @PutMapping("/{imageId}")
    @Operation(summary = "Update image metadata (Admin only)")
    public ResponseEntity<VehicleImageResponse> updateVehicleImage(
            @PathVariable Long imageId,
            @RequestBody VehicleImageRequest request) {
        VehicleImageResponse image = vehicleImageService.updateVehicleImage(imageId, request);
        return ResponseEntity.ok(image);
    }

    /**
     * Delete an image
     */
    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete an image (Admin only)")
    public ResponseEntity<Void> deleteVehicleImage(@PathVariable Long imageId) {
        vehicleImageService.deleteVehicleImage(imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all images in a category for a vehicle
     */
    @DeleteMapping("/vehicle/{vehicleId}/category/{category}")
    @Operation(summary = "Delete all images in a category for a vehicle (Admin only)")
    public ResponseEntity<Void> deleteVehicleImagesByCategory(
            @PathVariable Long vehicleId,
            @PathVariable String category) {
        vehicleImageService.deleteVehicleImagesByCategory(vehicleId, category);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set main image for a vehicle
     */
    @PostMapping("/vehicle/{vehicleId}/set-main/{imageId}")
    @Operation(summary = "Set main image for a vehicle (Admin only)")
    public ResponseEntity<Void> setMainImage(
            @PathVariable Long vehicleId,
            @PathVariable Long imageId) {
        vehicleImageService.setMainImage(vehicleId, imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder images within a category
     */
    @PostMapping("/vehicle/{vehicleId}/category/{category}/reorder")
    @Operation(summary = "Reorder images within a category (Admin only)")
    public ResponseEntity<Void> reorderImages(
            @PathVariable Long vehicleId,
            @PathVariable String category,
            @RequestBody List<Long> imageIds) {
        vehicleImageService.reorderImages(vehicleId, category, imageIds);
        return ResponseEntity.noContent().build();
    }
}
