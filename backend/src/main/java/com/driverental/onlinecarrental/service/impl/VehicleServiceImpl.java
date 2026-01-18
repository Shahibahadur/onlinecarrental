package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.VehicleRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.ImageStorageService;
import com.driverental.onlinecarrental.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ImageStorageService imageStorageService;

    private static final Pattern VEHICLE_IMAGE_WITH_CATEGORY = Pattern.compile("^/api/images/vehicles/([^/]+)/([^/]+)$");

    @Override
    public Page<VehicleResponse> getAllVehicles(Pageable pageable) {
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        return vehicles.map(v -> convertToResponse(v, false));
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        return convertToResponse(vehicle, true);
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        String imageName = null;
        String imageCategory = request.getType() != null ? request.getType().name().toLowerCase(Locale.ROOT) : "general";
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            String imageUrl = request.getImageUrl();
            int idx = imageUrl.indexOf("/api/images/vehicles/");
            if (idx > 0) {
                imageUrl = imageUrl.substring(idx);
            }

            Matcher m = VEHICLE_IMAGE_WITH_CATEGORY.matcher(imageUrl);
            if (m.matches()) {
                imageCategory = m.group(1);
                imageName = m.group(2);
            } else if (imageUrl.startsWith("/api/images/vehicles/")) {
                imageName = imageUrl.substring("/api/images/vehicles/".length());
                imageCategory = "general";
            } else if (request.getImageUrl().startsWith("http://") || request.getImageUrl().startsWith("https://")) {
                try {
                    imageName = imageStorageService.downloadVehicleImage(request.getImageUrl(), imageCategory);
                } catch (Exception e) {
                    imageName = null;
                }
            }
        }

        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .type(request.getType())
                .fuelType(request.getFuelType())
                .transmission(request.getTransmission())
                .seats(request.getSeats())
                .luggageCapacity(request.getLuggageCapacity())
                .features(request.getFeatures() != null ? request.getFeatures() : List.of())
                .basePrice(request.getBasePrice())
                .dailyPrice(request.getDailyPrice())
                .location(request.getLocation())
                .imageName(imageName)
                .imageCategory(imageName != null ? imageCategory : null)
                .isAvailable(request.getIsAvailable())
                .rating(0.0)
                .reviewCount(0)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(savedVehicle, false);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setType(request.getType());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setTransmission(request.getTransmission());
        vehicle.setSeats(request.getSeats());
        vehicle.setLuggageCapacity(request.getLuggageCapacity());
        vehicle.setFeatures(request.getFeatures() != null ? request.getFeatures() : List.of());
        vehicle.setBasePrice(request.getBasePrice());
        vehicle.setDailyPrice(request.getDailyPrice());
        vehicle.setLocation(request.getLocation());

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            String imageCategory = request.getType() != null ? request.getType().name().toLowerCase(Locale.ROOT) : "general";
            String imageUrl = request.getImageUrl();
            int idx = imageUrl.indexOf("/api/images/vehicles/");
            if (idx > 0) {
                imageUrl = imageUrl.substring(idx);
            }

            Matcher m = VEHICLE_IMAGE_WITH_CATEGORY.matcher(imageUrl);
            if (m.matches()) {
                String imageName = m.group(2);
                vehicle.setImageName(imageName);
                vehicle.setImageCategory(m.group(1));
                vehicle.setImageUrl(null);
            } else if (imageUrl.startsWith("/api/images/vehicles/")) {
                String imageName = imageUrl.substring("/api/images/vehicles/".length());
                vehicle.setImageName(imageName);
                vehicle.setImageCategory("general");
                vehicle.setImageUrl(null);
            } else if (request.getImageUrl().startsWith("http://") || request.getImageUrl().startsWith("https://")) {
                try {
                    String imageName = imageStorageService.downloadVehicleImage(request.getImageUrl(), imageCategory);
                    vehicle.setImageName(imageName);
                    vehicle.setImageCategory(imageCategory);
                    vehicle.setImageUrl(null);
                } catch (Exception e) {
                    vehicle.setImageUrl(null);
                }
            }
        }

        vehicle.setIsAvailable(request.getIsAvailable());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(updatedVehicle, false);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        vehicleRepository.delete(vehicle);
    }

    @Override
    public Page<VehicleResponse> getAvailableVehicles(Pageable pageable) {
        Page<Vehicle> vehicles = vehicleRepository.findByIsAvailableTrue(pageable);
        return vehicles.map(v -> convertToResponse(v, false));
    }

    @Override
    public Page<VehicleResponse> getAvailableVehicles(String search, Pageable pageable) {
        Page<Vehicle> vehicles = vehicleRepository.findAvailableBySearch(search, pageable);
        return vehicles.map(v -> convertToResponse(v, false));
    }

    @Override
    public VehicleResponse updateAvailability(Long id, Boolean isAvailable) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));

        vehicle.setIsAvailable(isAvailable);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(updatedVehicle, false);
    }

    private VehicleResponse convertToResponse(Vehicle vehicle, boolean migrateExternalImageOnRead) {
        if (migrateExternalImageOnRead
                && (vehicle.getImageName() == null || vehicle.getImageName().isBlank())
                && vehicle.getImageUrl() != null
                && (vehicle.getImageUrl().startsWith("http://") || vehicle.getImageUrl().startsWith("https://"))) {
            try {
                String category = vehicle.getType() != null ? vehicle.getType().name().toLowerCase(Locale.ROOT) : "general";
                String filename = imageStorageService.downloadVehicleImage(vehicle.getImageUrl(), category);
                vehicle.setImageName(filename);
                vehicle.setImageCategory(category);
                vehicle.setImageUrl(null);
                vehicleRepository.save(vehicle);
            } catch (Exception ignored) {
            }
        }

        String imageUrl = vehicle.getImageUrl();
        if (!migrateExternalImageOnRead
                && imageUrl != null
                && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            imageUrl = null;
        }
        if (vehicle.getImageName() != null && !vehicle.getImageName().isBlank()) {
            if (vehicle.getImageCategory() != null && !vehicle.getImageCategory().isBlank()) {
                imageUrl = "/api/images/vehicles/" + vehicle.getImageCategory() + "/" + vehicle.getImageName();
            } else {
                imageUrl = "/api/images/vehicles/" + vehicle.getImageName();
            }
        }
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
                .imageUrl(imageUrl)
                .isAvailable(vehicle.getIsAvailable())
                .rating(vehicle.getRating())
                .reviewCount(vehicle.getReviewCount())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}
