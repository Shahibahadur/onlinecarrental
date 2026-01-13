package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import com.driverental.onlinecarrental.model.dto.response.UserResponse;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.enums.BookingStatus;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.UserService;
import com.driverental.onlinecarrental.service.VehicleService;
import com.driverental.onlinecarrental.service.ReviewService;
import com.driverental.onlinecarrental.model.dto.response.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;

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
        // Calculate real statistics from repositories
        long totalUsers = userRepository.count();
        long totalCars = vehicleRepository.count();
        long totalBookings = bookingRepository.count();
        
        // Calculate revenue from completed bookings
        BigDecimal revenue = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> b.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Count bookings by status
        long activeBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.ACTIVE || 
                            b.getStatus() == BookingStatus.CONFIRMED)
                .count();
        
        long pendingBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .count();
        
        long completedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        
        // Count available vehicles
        long availableVehicles = vehicleRepository.findByIsAvailableTrue().size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalCars", totalCars);
        stats.put("totalBookings", totalBookings);
        stats.put("revenue", revenue.doubleValue());
        stats.put("activeBookings", activeBookings);
        stats.put("pendingBookings", pendingBookings);
        stats.put("completedBookings", completedBookings);
        stats.put("availableVehicles", availableVehicles);
        stats.put("unavailableVehicles", totalCars - availableVehicles);
        
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
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            BookingResponse booking = bookingService.getBookingById(id);
            
            switch (bookingStatus) {
                case CONFIRMED:
                    return ResponseEntity.ok(bookingService.confirmBooking(id));
                case CANCELLED:
                    return ResponseEntity.ok(bookingService.cancelBooking(id));
                case COMPLETED:
                case ACTIVE:
                    // For COMPLETED and ACTIVE, update directly via repository
                    com.driverental.onlinecarrental.model.entity.Booking bookingEntity = 
                        bookingRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Booking not found"));
                    bookingEntity.setStatus(bookingStatus);
                    bookingRepository.save(bookingEntity);
                    return ResponseEntity.ok(bookingService.getBookingById(id));
                default:
                    return ResponseEntity.ok(bookingService.confirmBooking(id));
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + status);
        }
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vehicles/{id}")
    @Operation(summary = "Delete vehicle (Admin only)")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/bookings/{id}/return")
    @Operation(summary = "Return a car (Admin only)")
    public ResponseEntity<BookingResponse> returnCar(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.returnCar(id));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get all reviews (Admin only)")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete review (Admin only)")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}