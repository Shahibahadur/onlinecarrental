package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import com.driverental.onlinecarrental.model.dto.response.UserResponse;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.UserService;
import com.driverental.onlinecarrental.service.VehicleService;
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
    private final VehicleService vehicleService;
    private final BookingService bookingService;

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Get all vehicles with admin details")
    public ResponseEntity<Page<VehicleResponse>> getAllVehiclesAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
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
            "totalVehicles", 45,
            "totalBookings", 320,
            "revenue", 45000.00,
            "activeBookings", 12
        );
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/vehicles/{id}/availability")
    @Operation(summary = "Update vehicle availability (Admin only)")
    public ResponseEntity<VehicleResponse> updateVehicleAvailability(
            @PathVariable Long id,
            @RequestParam Boolean available) {
        return ResponseEntity.ok(vehicleService.updateAvailability(id, available));
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