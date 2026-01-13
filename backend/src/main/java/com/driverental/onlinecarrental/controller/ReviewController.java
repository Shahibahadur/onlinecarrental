package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.ReviewRequest;
import com.driverental.onlinecarrental.model.dto.response.ReviewResponse;
import com.driverental.onlinecarrental.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review/Feedback management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewService.createReview(request, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get reviews for a vehicle")
    public ResponseEntity<Page<ReviewResponse>> getVehicleReviews(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getVehicleReviews(vehicleId, pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user")
    public ResponseEntity<Page<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getUserReviews(userId, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        Long userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vehicle/{vehicleId}/average-rating")
    @Operation(summary = "Get average rating for a vehicle")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long vehicleId) {
        Double averageRating = reviewService.getAverageRatingForVehicle(vehicleId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    private Long extractUserIdFromAuth(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof com.driverental.onlinecarrental.security.UserPrincipal) {
            com.driverental.onlinecarrental.security.UserPrincipal userPrincipal = (com.driverental.onlinecarrental.security.UserPrincipal) authentication
                    .getPrincipal();
            return userPrincipal.getId();
        }
        throw new com.driverental.onlinecarrental.model.exception.BusinessException("User not authenticated");
    }
}
