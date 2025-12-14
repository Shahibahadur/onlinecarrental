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

    Page<Review> findByCarId(Long carId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    Boolean existsByUserIdAndCarId(Long userId, Long carId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.car.id = :carId")
    Double findAverageRatingByCarId(@Param("carId") Long carId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.car.id = :carId")
    Long countByCarId(@Param("carId") Long carId);

    @Query("SELECT r FROM Review r WHERE r.car.id = :carId ORDER BY r.createdAt DESC")
    Page<Review> findLatestByCarId(@Param("carId") Long carId, Pageable pageable);
}