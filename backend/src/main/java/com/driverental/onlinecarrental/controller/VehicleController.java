package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.SearchCriteria;
import com.driverental.onlinecarrental.model.dto.request.VehicleRequest;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.service.SearchService;
import com.driverental.onlinecarrental.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Vehicle management APIs")
public class VehicleController {
    
    private final VehicleService vehicleService;
    private final SearchService searchService;
    
    @GetMapping
    @Operation(summary = "Get all vehicles with pagination")
    public ResponseEntity<Page<VehicleResponse>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }
    
    @PostMapping("/search")
    @Operation(summary = "Search vehicles with criteria")
    public ResponseEntity<Page<VehicleResponse>> searchVehicles(
            @RequestBody SearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.searchVehicles(criteria, pageable));
    }
    
    @GetMapping("/search/intelligent")
    @Operation(summary = "Intelligent search using Aho-Corasick algorithm")
    public ResponseEntity<List<VehicleResponse>> intelligentSearch(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        var results = searchService.intelligentSearch(query, location, pageable)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new vehicle (Admin only)")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.createVehicle(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update vehicle (Admin only)")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete vehicle (Admin only)")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }
    
    private VehicleResponse convertToResponse(com.driverental.onlinecarrental.model.entity.Vehicle vehicle) {
        // Implementation to convert entity to response DTO
        return new VehicleResponse(); // Placeholder
    }
}