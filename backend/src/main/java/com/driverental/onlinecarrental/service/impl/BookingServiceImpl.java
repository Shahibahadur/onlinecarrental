package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.BookingRequest;
import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.enums.BookingStatus;
import com.driverental.onlinecarrental.model.exception.BusinessException;
import com.driverental.onlinecarrental.model.exception.ResourceNotFoundException;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.BookingService;
import com.driverental.onlinecarrental.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PricingService pricingService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        if (!vehicle.getIsAvailable()) {
            throw new BusinessException("Vehicle is not available for booking");
        }

        // Parse and validate dates early
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(request.getStartDate());
            endDate = LocalDate.parse(request.getEndDate());
        } catch (Exception e) {
            log.error("Invalid date format. Start: {}, End: {}", request.getStartDate(), request.getEndDate());
            throw new BusinessException("Invalid date format. Please use YYYY-MM-DD");
        }

        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Start date cannot be in the past");
        }

        // Check vehicle availability with detailed logging
        if (!isVehicleAvailable(request.getVehicleId(), request.getStartDate(), request.getEndDate(), userId)) {
            log.warn("Vehicle {} not available for dates {} to {} (user: {})", 
                    request.getVehicleId(), startDate, endDate, userId);
            throw new BusinessException("Vehicle not available for selected dates. Please choose different dates.");
        }

        BigDecimal totalPrice = pricingService.calculateBookingPrice(vehicle, startDate, endDate);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .vehicle(vehicle)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: {} for vehicle: {}, dates: {} to {}", 
                savedBooking.getId(), vehicle.getId(), startDate, endDate);

        return convertToResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return convertToResponse(booking);
    }

    @Override
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);
        return bookings.map(this::convertToResponse);
    }

    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        return bookings.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Cannot cancel booking in current status: " + booking.getStatus());
        }

        // Check if booking starts within 24 hours (no cancellation allowed)
        if (booking.getStartDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new BusinessException("Cannot cancel booking within 24 hours of start date");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled: {}", id);

        return convertToResponse(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Booking cannot be confirmed in current status: " + booking.getStatus());
        }

        // Double-check vehicle availability (exclude this booking's user)
        if (!isVehicleAvailable(booking.getVehicle().getId(),
                booking.getStartDate().toString(),
                booking.getEndDate().toString(),
                booking.getUser().getId())) {
            throw new BusinessException("Vehicle no longer available for booking dates");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed: {}", id);

        return convertToResponse(updatedBooking);
    }

    @Override
    public boolean isVehicleAvailable(Long vehicleId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                vehicleId, start, end);

        if (!conflictingBookings.isEmpty()) {
            log.debug("Found {} conflicting bookings for vehicle {} on dates {} to {}", 
                    conflictingBookings.size(), vehicleId, start, end);
            conflictingBookings.forEach(b -> 
                    log.debug("  - Booking {}: {} ({}) to {} ({})", 
                            b.getId(), b.getUser().getEmail(), b.getStatus(), 
                            b.getEndDate(), b.getStatus()));
        }
        return conflictingBookings.isEmpty();
    }

    public boolean isVehicleAvailable(Long vehicleId, String startDate, String endDate, Long userId) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                vehicleId, start, end);

        // Filter out user's own PENDING bookings to allow retry/modification
        List<Booking> otherConflicts = conflictingBookings.stream()
                .filter(b -> !b.getUser().getId().equals(userId) || 
                           !b.getStatus().equals(BookingStatus.PENDING))
                .collect(Collectors.toList());

        if (!otherConflicts.isEmpty()) {
            log.warn("Vehicle {} not available for user {} on dates {} to {}: {} conflicts", 
                    vehicleId, userId, start, end, otherConflicts.size());
            otherConflicts.forEach(b -> 
                    log.debug("  - Booking {}: user={}, status={}, period: {} to {}", 
                            b.getId(), b.getUser().getEmail(), b.getStatus(), 
                            b.getStartDate(), b.getEndDate()));
        } else if (!conflictingBookings.isEmpty()) {
            log.info("User {} is retrying their own PENDING booking for vehicle {} on dates {} to {}", 
                    userId, vehicleId, start, end);
        }
        
        return otherConflicts.isEmpty();
    }

    @Transactional
    public void processExpiredBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                BookingStatus.PENDING, cutoffTime);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            log.info("Auto-cancelled expired booking: {}", booking.getId());
        }

        bookingRepository.saveAll(expiredBookings);
    }

    @Override
    @Transactional
    public BookingResponse returnCar(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.ACTIVE && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Cannot return car. Booking must be ACTIVE or CONFIRMED. Current status: " + booking.getStatus());
        }

        // Mark vehicle as available
        Vehicle vehicle = booking.getVehicle();
        vehicle.setIsAvailable(true);
        vehicleRepository.save(vehicle);

        // Update booking status to COMPLETED
        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Car returned successfully for booking: {}", bookingId);
        return convertToResponse(updatedBooking);
    }

    private BookingResponse convertToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .vehicleId(booking.getVehicle().getId())
                .startDate(booking.getStartDate().toString())
                .endDate(booking.getEndDate().toString())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .build();
    }
}