package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CollaborativeFiltering {
    
    public List<Vehicle> userBasedRecommendations(User targetUser, List<User> allUsers, 
                                                 List<Vehicle> allVehicles, int k) {
        Map<User, Double> userSimilarities = calculateUserSimilarities(targetUser, allUsers);
        
        // Get top K similar users
        List<User> similarUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Get vehicles booked by similar users but not by target user
        Set<Vehicle> targetUserVehicles = targetUser.getBookings().stream()
                .map(Booking::getVehicle)
                .collect(Collectors.toSet());
        
        Map<Vehicle, Integer> vehicleScores = new HashMap<>();
        for (User similarUser : similarUsers) {
            for (Booking booking : similarUser.getBookings()) {
                Vehicle vehicle = booking.getVehicle();
                if (!targetUserVehicles.contains(vehicle)) {
                    vehicleScores.put(vehicle, vehicleScores.getOrDefault(vehicle, 0) + 1);
                }
            }
        }
        
        return vehicleScores.entrySet().stream()
                .sorted(Map.Entry.<Vehicle, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public List<Vehicle> itemBasedRecommendations(User user, List<Vehicle> allVehicles, int k) {
        Set<Vehicle> userVehicles = user.getBookings().stream()
                .map(Booking::getVehicle)
                .collect(Collectors.toSet());
        
        Map<Vehicle, Double> vehicleSimilarities = new HashMap<>();
        
        for (Vehicle userVehicle : userVehicles) {
            for (Vehicle candidateVehicle : allVehicles) {
                if (!userVehicles.contains(candidateVehicle)) {
                    double similarity = calculateVehicleSimilarity(userVehicle, candidateVehicle);
                    vehicleSimilarities.merge(candidateVehicle, similarity, Double::sum);
                }
            }
        }
        
        return vehicleSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Vehicle, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    private Map<User, Double> calculateUserSimilarities(User targetUser, List<User> allUsers) {
        Map<User, Double> similarities = new HashMap<>();
        Set<Vehicle> targetUserVehicles = targetUser.getBookings().stream()
                .map(Booking::getVehicle)
                .collect(Collectors.toSet());
        
        for (User otherUser : allUsers) {
            if (otherUser.getId().equals(targetUser.getId())) continue;
            
            Set<Vehicle> otherUserVehicles = otherUser.getBookings().stream()
                    .map(Booking::getVehicle)
                    .collect(Collectors.toSet());
            
            double similarity = cosineSimilarity(targetUserVehicles, otherUserVehicles);
            similarities.put(otherUser, similarity);
        }
        
        return similarities;
    }
    
    private double calculateVehicleSimilarity(Vehicle v1, Vehicle v2) {
        double typeSimilarity = v1.getType().equals(v2.getType()) ? 1.0 : 0.0;
        double fuelSimilarity = v1.getFuelType().equals(v2.getFuelType()) ? 1.0 : 0.0;
        double priceSimilarity = 1.0 - Math.abs(v1.getDailyPrice().doubleValue() - 
                                               v2.getDailyPrice().doubleValue()) / 100.0;
        
        // Feature similarity using Jaccard index
        Set<String> features1 = new HashSet<>(v1.getFeatures());
        Set<String> features2 = new HashSet<>(v2.getFeatures());
        double featureSimilarity = jaccardSimilarity(features1, features2);
        
        return 0.3 * typeSimilarity + 0.2 * fuelSimilarity + 
               0.2 * priceSimilarity + 0.3 * featureSimilarity;
    }
    
    private double cosineSimilarity(Set<Vehicle> set1, Set<Vehicle> set2) {
        Set<Vehicle> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        if (intersection.isEmpty()) return 0.0;
        
        return intersection.size() / Math.sqrt(set1.size() * set2.size());
    }
    
    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
}