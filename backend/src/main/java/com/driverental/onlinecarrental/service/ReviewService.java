package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.ReviewRequest;
import com.driverental.onlinecarrental.model.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request, Long userId);

    ReviewResponse getReviewById(Long id);

    Page<ReviewResponse> getCarReviews(Long carId, Pageable pageable);

    Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable);

    ReviewResponse updateReview(Long id, ReviewRequest request);

    void deleteReview(Long id);

    Double getAverageRatingForCar(Long carId);
}