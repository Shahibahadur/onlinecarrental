package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.BookingRequest;
import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import com.driverental.onlinecarrental.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        // Extract user ID from authentication
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(bookingService.createBooking(request, userId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user ID")
    public ResponseEntity<Page<BookingResponse>> getUserBookings(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getUserBookings(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PostMapping("/{bookingId}/pay")
    @Operation(summary = "Process payment for booking")
    public ResponseEntity<BookingResponse> processPayment(@PathVariable Long bookingId) {
        // In a real implementation, this would integrate with payment gateway
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @GetMapping("/car/{carId}/availability")
    @Operation(summary = "Check car availability for dates")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long carId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(bookingService.isCarAvailable(carId, startDate, endDate));
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        // Implementation to extract user ID from authentication
        // This would typically come from JWT token
        return 1L; // Placeholder
    }
}