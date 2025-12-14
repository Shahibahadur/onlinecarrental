package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.dto.request.ReviewRequest;
import com.driverental.onlinecarrental.model.dto.response.ReviewResponse;
import com.driverental.onlinecarrental.model.entity.Review;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.repository.ReviewRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.CarRepository;
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
    private final CarRepository carRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        // Check if user has already reviewed this car
        if (reviewRepository.existsByUserIdAndCarId(userId, request.getCarId())) {
            throw new RuntimeException("User has already reviewed this car");
        }

        Review review = Review.builder()
                .user(user)
                .car(car)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update car rating
        updateCarRating(car);

        return convertToResponse(savedReview);
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return convertToResponse(review);
    }

    @Override
    public Page<ReviewResponse> getCarReviews(Long carId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByCarId(carId, pageable);
        return reviews.map(this::convertToResponse);
    }

    @Override
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(this::convertToResponse);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepository.save(review);

        // Update car rating
        updateCarRating(review.getCar());

        return convertToResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Car car = review.getCar();
        reviewRepository.delete(review);

        // Update car rating
        updateCarRating(car);
    }

    @Override
    public Double getAverageRatingForCar(Long carId) {
        return reviewRepository.findAverageRatingByCarId(carId);
    }

    private void updateCarRating(Car car) {
        Double averageRating = reviewRepository.findAverageRatingByCarId(car.getId());
        Long reviewCount = reviewRepository.countByCarId(car.getId());

        car.setRating(averageRating != null ? averageRating : 0.0);
        car.setReviewCount(reviewCount.intValue());

        carRepository.save(car);
    }

    private ReviewResponse convertToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .carId(review.getCar().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}