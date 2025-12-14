package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.BookingRequest;
import com.driverental.onlinecarrental.model.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, Long userId);

    BookingResponse getBookingById(Long id);

    Page<BookingResponse> getUserBookings(Long userId, Pageable pageable);

    BookingResponse cancelBooking(Long id);

    BookingResponse confirmBooking(Long id);

    boolean isCarAvailable(Long carId, String startDate, String endDate);

    Page<BookingResponse> getAllBookings(Pageable pageable);
}