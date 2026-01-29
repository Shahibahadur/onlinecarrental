package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.VehicleImageRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleImageResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.entity.VehicleImage;
import com.driverental.onlinecarrental.repository.VehicleImageRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.VehicleImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleImageServiceImpl implements VehicleImageService {

    private final VehicleImageRepository vehicleImageRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${app.storage.vehicles-dir:../uploads/vehicles}")
    private String vehiclesDir;

    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Override
    public List<VehicleImageResponse> getVehicleImages(Long vehicleId) {
        return vehicleImageRepository.findByVehicleIdAndIsActiveTrue(vehicleId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleImageResponse> getVehicleImagesByCategory(Long vehicleId, String category) {
        return vehicleImageRepository.findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(vehicleId, category)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleImageResponse addVehicleImage(VehicleImageRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + request.getVehicleId()));

        VehicleImage image = VehicleImage.builder()
                .vehicle(vehicle)
                .imageName(request.getImageName())
                .category(request.getCategory() != null ? request.getCategory() : "SEDAN")
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .altText(request.getAltText())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        VehicleImage saved = vehicleImageRepository.save(image);
        return convertToResponse(saved);
    }

    @Override
    public VehicleImageResponse updateVehicleImage(Long imageId, VehicleImageRequest request) {
        VehicleImage image = vehicleImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        if (request.getImageName() != null) {
            image.setImageName(request.getImageName());
        }
        if (request.getCategory() != null) {
            image.setCategory(request.getCategory());
        }
        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getAltText() != null) {
            image.setAltText(request.getAltText());
        }
        if (request.getDescription() != null) {
            image.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            image.setIsActive(request.getIsActive());
        }

        VehicleImage updated = vehicleImageRepository.save(image);
        return convertToResponse(updated);
    }

    @Override
    public void deleteVehicleImage(Long imageId) {
        vehicleImageRepository.deleteById(imageId);
    }

    @Override
    public void deleteVehicleImagesByCategory(Long vehicleId, String category) {
        vehicleImageRepository.deleteByVehicleIdAndCategory(vehicleId, category);
    }

    @Override
    public void setMainImage(Long vehicleId, Long imageId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        VehicleImage image = vehicleImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        if (!image.getVehicle().getId().equals(vehicleId)) {
            throw new RuntimeException("Image does not belong to this vehicle");
        }

        // Update vehicle's main image name
        vehicle.setMainImageName(image.getImageName());
        vehicleRepository.save(vehicle);

        // Set this image's display order to 0
        image.setDisplayOrder(0);
        vehicleImageRepository.save(image);
    }

    @Override
    public void reorderImages(Long vehicleId, String category, List<Long> imageIds) {
        for (int i = 0; i < imageIds.size(); i++) {
            final int index = i;
            Long imageId = imageIds.get(i);
            VehicleImage image = vehicleImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

            if (!image.getVehicle().getId().equals(vehicleId)) {
                throw new RuntimeException("Image does not belong to this vehicle");
            }

            image.setDisplayOrder(index);
            vehicleImageRepository.save(image);
        }
    }

    @Override
    public VehicleImageResponse getMainImage(Long vehicleId) {
        VehicleImage mainImage = vehicleImageRepository.findByVehicleIdAndCategoryAndIsActiveTrue(vehicleId, "MAIN");
        return mainImage != null ? convertToResponse(mainImage) : null;
    }

    @Override
    public long countVehicleImages(Long vehicleId) {
        return vehicleImageRepository.countByVehicleIdAndIsActiveTrue(vehicleId);
    }

    private VehicleImageResponse convertToResponse(VehicleImage image) {
        String imageUrl = "/api/images/vehicles/categorized/" + image.getCategory().toLowerCase() 
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
