package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.VehicleRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Page<VehicleResponse> getAllVehicles(Pageable pageable) {
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        return vehicles.map(this::convertToResponse);
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        return convertToResponse(vehicle);
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
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
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable())
                .rating(0.0)
                .reviewCount(0)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(savedVehicle);
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
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setIsAvailable(request.getIsAvailable());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(updatedVehicle);
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
        return vehicles.map(this::convertToResponse);
    }

    @Override
    public VehicleResponse updateAvailability(Long id, Boolean isAvailable) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        
        vehicle.setIsAvailable(isAvailable);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToResponse(updatedVehicle);
    }

    private VehicleResponse convertToResponse(Vehicle vehicle) {
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
}