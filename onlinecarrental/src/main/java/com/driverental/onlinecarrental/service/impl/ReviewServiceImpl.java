package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.ReviewRequest;
import com.driverental.onlinecarrental.model.dto.response.ReviewResponse;
import com.driverental.onlinecarrental.model.entity.Review;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.ReviewRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Check if user has already reviewed this vehicle
        if (reviewRepository.existsByUserIdAndVehicleId(userId, request.getVehicleId())) {
            throw new RuntimeException("User has already reviewed this vehicle");
        }

        Review review = Review.builder()
                .user(user)
                .vehicle(vehicle)
                .rating(request.getRating().intValue())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update vehicle rating
        updateVehicleRating(vehicle);

        return convertToResponse(savedReview);
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToResponse(review);
    }

    @Override
    public Page<ReviewResponse> getVehicleReviews(Long vehicleId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByVehicleId(vehicleId, pageable);
        return reviews.map(this::convertToResponse);
    }

    @Override
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Vehicle vehicle = review.getVehicle();
        reviewRepository.delete(review);

        // Update vehicle rating
        updateVehicleRating(vehicle);
    }

    @Override
    public Double getAverageRatingForVehicle(Long vehicleId) {
        return reviewRepository.findAverageRatingByVehicleId(vehicleId);
    }

    private void updateVehicleRating(Vehicle vehicle) {
        Double averageRating = reviewRepository.findAverageRatingByVehicleId(vehicle.getId());
        Long reviewCount = reviewRepository.countByVehicleId(vehicle.getId());

        vehicle.setRating(averageRating != null ? averageRating : 0.0);
        vehicle.setReviewCount(reviewCount.intValue());

        vehicleRepository.save(vehicle);
    }

    private ReviewResponse convertToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .vehicleId(review.getVehicle().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getVehicle().getId().equals(request.getVehicleId())) {
            throw new RuntimeException("Cannot change vehicle for a review");
        }

        review.setRating(request.getRating().intValue());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);
        updateVehicleRating(review.getVehicle());

        return convertToResponse(updatedReview);
    }

    @Override
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(this::convertToResponse);
    }
}