package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByVehicleId(Long vehicleId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
    Boolean existsByUserIdAndVehicleId(Long userId, Long vehicleId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.vehicle.id = :vehicleId")
    Double findAverageRatingByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.vehicle.id = :vehicleId")
    Long countByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId ORDER BY r.createdAt DESC")
    Page<Review> findLatestByVehicleId(@Param("vehicleId") Long vehicleId, Pageable pageable);
}