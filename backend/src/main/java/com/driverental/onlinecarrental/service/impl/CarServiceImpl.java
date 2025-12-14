package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.CarRequest;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.repository.CarRepository;
import com.driverental.onlinecarrental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public Page<CarResponse> getAllCars(Pageable pageable) {
        Page<Car> cars = carRepository.findAll(pageable);
        return cars.map(this::convertToResponse);
    }

    @Override
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
        return convertToResponse(car);
    }

    @Override
    public CarResponse createCar(CarRequest request) {
        Car car = Car.builder()
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

        Car savedCar = carRepository.save(car);
        return convertToResponse(savedCar);
    }

    @Override
    public CarResponse updateCar(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));

        car.setMake(request.getMake());
        car.setModel(request.getModel());
        car.setYear(request.getYear());
        car.setType(request.getType());
        car.setFuelType(request.getFuelType());
        car.setTransmission(request.getTransmission());
        car.setSeats(request.getSeats());
        car.setLuggageCapacity(request.getLuggageCapacity());
        car.setFeatures(request.getFeatures() != null ? request.getFeatures() : List.of());
        car.setBasePrice(request.getBasePrice());
        car.setDailyPrice(request.getDailyPrice());
        car.setLocation(request.getLocation());
        car.setImageUrl(request.getImageUrl());
        car.setIsAvailable(request.getIsAvailable());

        Car updatedCar = carRepository.save(car);
        return convertToResponse(updatedCar);
    }

    @Override
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
        carRepository.delete(car);
    }

    @Override
    public Page<CarResponse> getAvailableCars(Pageable pageable) {
        Page<Car> cars = carRepository.findByIsAvailableTrue(pageable);
        return cars.map(this::convertToResponse);
    }

    @Override
    public CarResponse updateAvailability(Long id, Boolean isAvailable) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));

        car.setIsAvailable(isAvailable);
        Car updatedCar = carRepository.save(car);
        return convertToResponse(updatedCar);
    }

    private CarResponse convertToResponse(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .year(car.getYear())
                .type(car.getType())
                .fuelType(car.getFuelType())
                .transmission(car.getTransmission())
                .seats(car.getSeats())
                .luggageCapacity(car.getLuggageCapacity())
                .features(car.getFeatures())
                .basePrice(car.getBasePrice())
                .dailyPrice(car.getDailyPrice())
                .location(car.getLocation())
                .imageUrl(car.getImageUrl())
                .isAvailable(car.getIsAvailable())
                .rating(car.getRating())
                .reviewCount(car.getReviewCount())
                .createdAt(car.getCreatedAt())
                .build();
    }
}