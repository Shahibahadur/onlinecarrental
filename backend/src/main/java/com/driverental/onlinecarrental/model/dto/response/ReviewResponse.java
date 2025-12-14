package com.driverental.onlinecarrental.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long userId;
    private Long carId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private CarResponse car; // Optional: include car details
}