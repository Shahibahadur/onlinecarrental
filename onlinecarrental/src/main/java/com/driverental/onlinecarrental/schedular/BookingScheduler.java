package com.driverental.onlinecarrental.schedular;

import com.driverental.onlinecarrental.service.impl.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {
    
    private final BookingServiceImpl bookingService;
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void processExpiredBookings() {
        log.info("Starting expired bookings processing...");
        try {
            bookingService.processExpiredBookings();
            log.info("Expired bookings processing completed successfully");
        } catch (Exception e) {
            log.error("Error processing expired bookings", e);
        }
    }
    
    @Scheduled(cron = "0 0 6 * * ?") // Run daily at 6 AM
    public void updateBookingStatuses() {
        log.info("Starting booking status updates...");
        // This would update booking statuses from CONFIRMED to ACTIVE, ACTIVE to COMPLETED, etc.
        log.info("Booking status updates completed");
    }
}