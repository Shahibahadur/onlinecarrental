package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.VehicleImageResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.entity.VehicleImage;
import com.driverental.onlinecarrental.repository.VehicleImageRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/images/vehicles")
@RequiredArgsConstructor
@Tag(name = "Admin Images", description = "Admin-only image management APIs")
public class AdminVehicleImageController {

    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;
    private final ImageStorageService imageStorageService;

    @Value("${app.storage.vehicles-dir:uploads/vehicles}")
    private String vehiclesDir;

    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    private Path getVehiclesBasePath() {
        Path base = Path.of(vehiclesDir);
        if (!base.isAbsolute()) {
            Path cwd = Path.of("").toAbsolutePath().normalize();
            base = cwd.resolve(vehiclesDir).normalize();
        }
        return base;
    }

    public record MigrationResult(int total, int migrated, int skipped, int failed, List<Long> failedVehicleIds) {
    }

    public record ImageListResponse(Long vehicleId, String vehicleInfo, List<VehicleImageResponse> images) {
    }

    public record ImageDeleteResponse(boolean success, String message, Long imageId, String fileName) {
    }

    /**
     * Get all images organized by vehicle and category
     */
    @GetMapping("/list")
    @Operation(summary = "List all vehicle images organized by category (Admin only)")
    public ResponseEntity<List<ImageListResponse>> listAllVehicleImages(
            @RequestParam(value = "vehicleId", required = false) Long vehicleId,
            @RequestParam(value = "category", required = false) String category) {
        
        List<ImageListResponse> responses = new ArrayList<>();
        
        if (vehicleId != null) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
            
            List<VehicleImage> images;
            if (category != null) {
                images = vehicleImageRepository.findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(vehicleId, category);
            } else {
                images = vehicleImageRepository.findByVehicleIdAndIsActiveTrue(vehicleId);
            }
            
            responses.add(new ImageListResponse(
                    vehicleId,
                    vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")",
                    images.stream().map(this::convertToResponse).collect(Collectors.toList())
            ));
        } else {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            for (Vehicle vehicle : vehicles) {
                List<VehicleImage> images;
                if (category != null) {
                    images = vehicleImageRepository.findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(vehicle.getId(), category);
                } else {
                    images = vehicleImageRepository.findByVehicleIdAndIsActiveTrue(vehicle.getId());
                }
                
                if (!images.isEmpty()) {
                    responses.add(new ImageListResponse(
                            vehicle.getId(),
                            vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")",
                            images.stream().map(this::convertToResponse).collect(Collectors.toList())
                    ));
                }
            }
        }
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Upload and add image for a vehicle by category
     */
    @PostMapping("/upload/{vehicleId}/{category}")
    @Operation(summary = "Upload image for vehicle by category (Admin only)")
        public ResponseEntity<VehicleImageResponse> uploadVehicleImageByCategory(
            @PathVariable Long vehicleId,
            @PathVariable String category,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "vehicleName", required = false) String vehicleName,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "description", required = false) String description) throws Exception {
        
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        String categoryName = category.toLowerCase();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        
        // Extract file extension
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0 && dot < originalFilename.length() - 1) {
            ext = originalFilename.substring(dot);
        } else {
            ext = ".jpg";
        }

        // Determine filename: prefer provided vehicleName, then vehicle make+model, then registrationNumber, then id
        String filename;
        if (vehicleName != null && !vehicleName.isBlank()) {
            filename = vehicleName.replaceAll("[^a-zA-Z0-9_-]", "_") + ext;
        } else if (vehicle.getMake() != null && vehicle.getModel() != null && !(vehicle.getMake().isBlank() || vehicle.getModel().isBlank())) {
            filename = (vehicle.getMake() + "_" + vehicle.getModel()).replaceAll("[^a-zA-Z0-9_-]", "_") + ext;
        } else if (vehicle.getRegistrationNumber() != null && !vehicle.getRegistrationNumber().isBlank()) {
            filename = vehicle.getRegistrationNumber().replaceAll("[^a-zA-Z0-9_-]", "_") + ext;
        } else {
            filename = vehicle.getId() + ext;
        }

        // Create category-specific directory
        Path dir = getVehiclesBasePath().resolve(categoryName);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Find max display order
        List<VehicleImage> existingImages = vehicleImageRepository.findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(vehicleId, category);
        int displayOrder = existingImages.isEmpty() ? 0 : existingImages.get(existingImages.size() - 1).getDisplayOrder() + 1;

        VehicleImage image = VehicleImage.builder()
                .vehicle(vehicle)
                .imageName(filename)
                .category(category)
                .displayOrder(displayOrder)
                .altText(altText != null ? altText : originalFilename)
                .description(description)
                .isActive(true)
                .build();

        VehicleImage saved = vehicleImageRepository.save(image);
        return ResponseEntity.ok(convertToResponse(saved));
    }

    /**
     * Delete image and remove file from storage
     */
    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete image and remove from storage (Admin only)")
    public ResponseEntity<ImageDeleteResponse> deleteImage(@PathVariable Long imageId) {
        VehicleImage image = vehicleImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        String imageName = image.getImageName();
        String category = image.getCategory().toLowerCase();
        
        try {
            // Delete file from storage
            Path filePath = getVehiclesBasePath().resolve(category).resolve(imageName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // Delete from database
            vehicleImageRepository.deleteById(imageId);
            
            return ResponseEntity.ok(new ImageDeleteResponse(
                    true,
                    "Image deleted successfully",
                    imageId,
                    imageName
            ));
        } catch (IOException e) {
            return ResponseEntity.ok(new ImageDeleteResponse(
                    false,
                    "Failed to delete image: " + e.getMessage(),
                    imageId,
                    imageName
            ));
        }
    }

    /**
     * List all uploaded images by category (file system based)
     */
    @GetMapping("/filesystem/by-category")
    @Operation(summary = "List all uploaded images organized by category from file system")
    public ResponseEntity<Map<String, List<String>>> listImagesByCategory() throws IOException {
        Map<String, List<String>> categoryImages = new TreeMap<>();
        
        Path vehiclesPath = getVehiclesBasePath();
        if (!Files.exists(vehiclesPath)) {
            return ResponseEntity.ok(categoryImages);
        }
        
        try (var stream = Files.list(vehiclesPath)) {
            stream.filter(Files::isDirectory)
                    .forEach(categoryPath -> {
                        String categoryName = categoryPath.getFileName().toString();
                        try (var imageStream = Files.list(categoryPath)) {
                            List<String> images = imageStream
                                    .filter(Files::isRegularFile)
                                    .map(p -> p.getFileName().toString())
                                    .sorted()
                                    .collect(Collectors.toList());
                            if (!images.isEmpty()) {
                                categoryImages.put(categoryName, images);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        
        return ResponseEntity.ok(categoryImages);
    }

    /**
     * Get detailed count of images
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get image statistics by category")
    public ResponseEntity<Map<String, Object>> getImageStatistics() throws IOException {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        // Database stats
        Map<String, Long> categoryStats = new LinkedHashMap<>();
        vehicleImageRepository.findAll().stream()
                .collect(Collectors.groupingBy(VehicleImage::getCategory, Collectors.counting()))
                .forEach(categoryStats::put);
        stats.put("categoryStats", categoryStats);
        stats.put("totalImages", vehicleImageRepository.count());
        
        // Vehicle stats
        Map<String, Long> vehicleStats = vehicleRepository.findAll().stream()
                .collect(Collectors.toMap(
                        v -> v.getMake() + " " + v.getModel(),
                        v -> vehicleImageRepository.countByVehicleIdAndIsActiveTrue(v.getId())
                ));
        stats.put("vehicleStats", vehicleStats);
        
        // File system stats
        Path vehiclesPath = Path.of(vehiclesDir);
        if (Files.exists(vehiclesPath)) {
            Map<String, Long> filesystemStats = new LinkedHashMap<>();
            try (var stream = Files.list(vehiclesPath)) {
                stream.filter(Files::isDirectory)
                        .forEach(categoryPath -> {
                            String categoryName = categoryPath.getFileName().toString();
                            try (var imageStream = Files.list(categoryPath)) {
                                long count = imageStream.filter(Files::isRegularFile).count();
                                if (count > 0) {
                                    filesystemStats.put(categoryName, count);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            stats.put("filesystemStats", filesystemStats);
        }
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/migrate")
    @Operation(summary = "Bulk migrate vehicle images from external URLs into local uploads (Admin only)")
    public ResponseEntity<MigrationResult> migrateVehicleImages(
            @RequestParam(value = "max", required = false) Integer max
    ) {
        // Migration of external image URLs is not supported because the Vehicle entity
        // no longer stores external `imageUrl` values. Return skipped for all entries.
        List<Vehicle> vehicles = vehicleRepository.findAll();
        int limit = max != null && max > 0 ? Math.min(max, vehicles.size()) : vehicles.size();
        int migrated = 0;
        int skipped = limit;
        int failed = 0;
        List<Long> failedIds = new ArrayList<>();

        return ResponseEntity.ok(new MigrationResult(limit, migrated, skipped, failed, failedIds));
    }

    private VehicleImageResponse convertToResponse(VehicleImage image) {
        String imageUrl = apiBaseUrl + "/api/images/vehicles/" + image.getCategory().toLowerCase() 
                + "/" + image.getImageName();

        return VehicleImageResponse.builder()
                .id(image.getId())
                .vehicleId(image.getVehicle().getId())
                .imageName(image.getImageName())
                .imageUrl(imageUrl)
                .category(image.getCategory())
                .displayOrder(image.getDisplayOrder())
                .altText(image.getAltText())
                .description(image.getDescription())
                .isActive(image.getIsActive())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
