package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import com.driverental.onlinecarrental.model.dto.response.UserResponse;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.UserService;
import com.driverental.onlinecarrental.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {

    private final UserService userService;
    private final CarService carService;
    private final BookingService bookingService;

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/cars")
    @Operation(summary = "Get all cars with admin details")
    public ResponseEntity<Page<CarResponse>> getAllCarsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(carService.getAllCars(pageable));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get all bookings (Admin only)")
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // This would typically call a dedicated service for dashboard statistics
        Map<String, Object> stats = Map.of(
                "totalUsers", 150,
                "totalCars", 45,
                "totalBookings", 320,
                "revenue", 45000.00,
                "activeBookings", 12);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/cars/{id}/availability")
    @Operation(summary = "Update car availability (Admin only)")
    public ResponseEntity<CarResponse> updateCarAvailability(
            @PathVariable Long id,
            @RequestParam Boolean available) {
        return ResponseEntity.ok(carService.updateAvailability(id, available));
    }

    @PutMapping("/bookings/{id}/status")
    @Operation(summary = "Update booking status (Admin only)")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        // Implementation would handle different status updates
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }
}