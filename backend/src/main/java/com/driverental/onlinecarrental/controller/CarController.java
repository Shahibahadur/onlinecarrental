package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.SearchCriteria;
import com.driverental.onlinecarrental.model.dto.request.CarRequest;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import com.driverental.onlinecarrental.service.SearchService;
import com.driverental.onlinecarrental.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Car", description = "Car management APIs")
public class CarController {

    private final CarService carService;
    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Get all cars with pagination")
    public ResponseEntity<Page<CarResponse>> getAllCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(carService.getAllCars(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search cars with criteria")
    public ResponseEntity<Page<CarResponse>> searchCars(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.searchCars(criteria, pageable));
    }

    @GetMapping("/search/intelligent")
    @Operation(summary = "Intelligent search using Aho-Corasick algorithm")
    public ResponseEntity<List<CarResponse>> intelligentSearch(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.intelligentSearch(query, location, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new car (Admin only)")
    public ResponseEntity<CarResponse> createCar(@Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(carService.createCar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update car (Admin only)")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable Long id, @Valid @RequestBody CarRequest request) {
        return ResponseEntity.ok(carService.updateCar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete car (Admin only)")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }

}