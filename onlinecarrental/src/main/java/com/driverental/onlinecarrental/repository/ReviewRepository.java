package com.driverental.onlinecarrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.driverental.onlinecarrental.model.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
