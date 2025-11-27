package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MatrixFactorization {
    
    private static final int LATENT_FEATURES = 10;
    private static final double LEARNING_RATE = 0.01;
    private static final double REGULARIZATION = 0.02;
    private static final int ITERATIONS = 100;
    
    public Map<Long, Double> factorize(List<User> users, List<Vehicle> vehicles, 
                                      Map<Long, Map<Long, Double>> userItemRatings) {
        int numUsers = users.size();
        int numItems = vehicles.size();
        
        // Initialize user and item matrices
        Matrix userFeatures = new DenseMatrix(numUsers, LATENT_FEATURES);
        Matrix itemFeatures = new DenseMatrix(numItems, LATENT_FEATURES);
        
        Random random = new Random();
        for (int i = 0; i < numUsers; i++) {
            for (int j = 0; j < LATENT_FEATURES; j++) {
                userFeatures.set(i, j, random.nextDouble() * 0.1);
            }
        }
        
        for (int i = 0; i < numItems; i++) {
            for (int j = 0; j < LATENT_FEATURES; j++) {
                itemFeatures.set(i, j, random.nextDouble() * 0.1);
            }
        }
        
        // Stochastic Gradient Descent
        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (User user : users) {
                for (Vehicle vehicle : vehicles) {
                    Double actualRating = userItemRatings
                            .getOrDefault(user.getId(), new HashMap<>())
                            .get(vehicle.getId());
                    
                    if (actualRating != null) {
                        int userIndex = users.indexOf(user);
                        int itemIndex = vehicles.indexOf(vehicle);
                        
                        double predictedRating = predictRating(userFeatures, itemFeatures, userIndex, itemIndex);
                        double error = actualRating - predictedRating;
                        
                        // Update user and item features
                        for (int f = 0; f < LATENT_FEATURES; f++) {
                            double userFeature = userFeatures.get(userIndex, f);
                            double itemFeature = itemFeatures.get(itemIndex, f);
                            
                            userFeatures.set(userIndex, f, 
                                userFeature + LEARNING_RATE * 
                                (error * itemFeature - REGULARIZATION * userFeature));
                            
                            itemFeatures.set(itemIndex, f,
                                itemFeature + LEARNING_RATE *
                                (error * userFeature - REGULARIZATION * itemFeature));
                        }
                    }
                }
            }
        }
        
        // Generate predictions
        Map<Long, Double> predictions = new HashMap<>();
        for (User user : users) {
            for (Vehicle vehicle : vehicles) {
                int userIndex = users.indexOf(user);
                int itemIndex = vehicles.indexOf(vehicle);
                double predictedRating = predictRating(userFeatures, itemFeatures, userIndex, itemIndex);
                predictions.put(vehicle.getId(), predictedRating);
            }
        }
        
        return predictions;
    }
    
    private double predictRating(Matrix userFeatures, Matrix itemFeatures, int userIndex, int itemIndex) {
        double prediction = 0.0;
        for (int f = 0; f < LATENT_FEATURES; f++) {
            prediction += userFeatures.get(userIndex, f) * itemFeatures.get(itemIndex, f);
        }
        return Math.min(5.0, Math.max(1.0, prediction)); // Clip to 1-5 rating scale
    }
    
    public Map<Long, Map<Long, Double>> buildUserItemMatrix(List<User> users, List<Booking> bookings) {
        Map<Long, Map<Long, Double>> userItemRatings = new HashMap<>();
        
        for (User user : users) {
            Map<Long, Double> itemRatings = new HashMap<>();
            
            // Calculate implicit ratings from booking behavior
            for (Booking booking : user.getBookings()) {
                double rating = calculateImplicitRating(booking);
                itemRatings.put(booking.getVehicle().getId(), rating);
            }
            
            userItemRatings.put(user.getId(), itemRatings);
        }
        
        return userItemRatings;
    }
    
    private double calculateImplicitRating(Booking booking) {
        double baseRating = 3.0; // Base rating for any booking
        
        // Adjust based on booking duration (longer duration = higher preference)
        long days = booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay();
        double durationBonus = Math.min(2.0, days / 7.0); // Max 2 points bonus
        
        // Adjust based on repeat bookings (not implemented here)
        
        return Math.min(5.0, baseRating + durationBonus);
    }
}