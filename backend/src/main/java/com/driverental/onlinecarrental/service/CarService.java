package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.CarRequest;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    Page<CarResponse> getAllCars(Pageable pageable);

    CarResponse getCarById(Long id);

    CarResponse createCar(CarRequest request);

    CarResponse updateCar(Long id, CarRequest request);

    void deleteCar(Long id);

    Page<CarResponse> getAvailableCars(Pageable pageable);

    CarResponse updateAvailability(Long id, Boolean isAvailable);
}