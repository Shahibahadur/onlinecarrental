package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    Page<Booking> findByVehicleId(Long vehicleId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId AND " +
           "b.status IN :statuses AND " +
           "((b.startDate BETWEEN :startDate AND :endDate) OR " +
           "(b.endDate BETWEEN :startDate AND :endDate) OR " +
           "(b.startDate <= :startDate AND b.endDate >= :endDate))")
    List<Booking> findConflictingBookings(@Param("vehicleId") Long vehicleId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.startDate BETWEEN :start AND :end")
    Long countByStartDateBetween(@Param("start") LocalDate start, 
                                @Param("end") LocalDate end);
    
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);
}