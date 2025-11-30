package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.VehicleRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    Page<VehicleResponse> getAllVehicles(Pageable pageable);
    VehicleResponse getVehicleById(Long id);
    VehicleResponse createVehicle(VehicleRequest request);
    VehicleResponse updateVehicle(Long id, VehicleRequest request);
    void deleteVehicle(Long id);
    Page<VehicleResponse> getAvailableVehicles(Pageable pageable);
    VehicleResponse updateAvailability(Long id, Boolean isAvailable);
}