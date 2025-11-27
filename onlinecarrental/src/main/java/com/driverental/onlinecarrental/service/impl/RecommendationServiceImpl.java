package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.recommendation.HybridRecommender;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    private final HybridRecommender hybridRecommender;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    
    @Override
    @Cacheable(value = "userRecommendations", key = "#userId")
    public List<Vehicle> getRecommendationsForUser(Long userId) {
        log.info("Generating recommendations for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<User> allUsers = userRepository.findAll();
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        
        return hybridRecommender.getHybridRecommendations(user, allUsers, allVehicles, 10);
    }
    
    @Override
    public List<Vehicle> getPopularVehicles() {
        return vehicleRepository.findTop10ByOrderByRatingDescReviewCountDesc();
    }
}