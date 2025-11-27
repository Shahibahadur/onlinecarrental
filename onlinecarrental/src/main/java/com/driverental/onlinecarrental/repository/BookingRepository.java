package com.driverental.onlinecarrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.driverental.onlinecarrental.model.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
