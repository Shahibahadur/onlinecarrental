package com.driverental.onlinecarrental.algorithm.recommendation;

import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Car;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
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

    public Map<Long, Double> factorize(List<User> users, List<Car> cars,
            Map<Long, Map<Long, Double>> userItemRatings) {
        int numUsers = users.size();
        int numItems = cars.size();

        // Initialize user and item matrices
        RealMatrix userFeatures = new Array2DRowRealMatrix(numUsers, LATENT_FEATURES);
        RealMatrix itemFeatures = new Array2DRowRealMatrix(numItems, LATENT_FEATURES);

        Random random = new Random();
        for (int i = 0; i < numUsers; i++) {
            for (int j = 0; j < LATENT_FEATURES; j++) {
                userFeatures.setEntry(i, j, random.nextDouble() * 0.1);
            }
        }

        for (int i = 0; i < numItems; i++) {
            for (int j = 0; j < LATENT_FEATURES; j++) {
                itemFeatures.setEntry(i, j, random.nextDouble() * 0.1);
            }
        }

        // Stochastic Gradient Descent
        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (User user : users) {
                for (Car car : cars) {
                    Double actualRating = userItemRatings
                            .getOrDefault(user.getId(), new HashMap<>())
                            .get(car.getId());

                    if (actualRating != null) {
                        int userIndex = users.indexOf(user);
                        int itemIndex = cars.indexOf(car);

                        double predictedRating = predictRating(userFeatures, itemFeatures, userIndex, itemIndex);
                        double error = actualRating - predictedRating;

                        // Update user and item features
                        for (int f = 0; f < LATENT_FEATURES; f++) {
                            double userFeature = userFeatures.getEntry(userIndex, f);
                            double itemFeature = itemFeatures.getEntry(itemIndex, f);

                            double newUserFeature = userFeature + LEARNING_RATE *
                                    (error * itemFeature - REGULARIZATION * userFeature);
                            userFeatures.setEntry(userIndex, f, newUserFeature);

                            double newItemFeature = itemFeature + LEARNING_RATE *
                                    (error * userFeature - REGULARIZATION * itemFeature);
                            itemFeatures.setEntry(itemIndex, f, newItemFeature);
                        }
                    }
                }
            }

            if (iter % 10 == 0) {
                log.debug("Completed iteration {}/{}", iter, ITERATIONS);
            }
        }

        // Generate predictions
        Map<Long, Double> predictions = new HashMap<>();
        for (User user : users) {
            for (Car car : cars) {
                int userIndex = users.indexOf(user);
                int itemIndex = cars.indexOf(car);
                double predictedRating = predictRating(userFeatures, itemFeatures, userIndex, itemIndex);
                predictions.put(car.getId(), predictedRating);
            }
        }

        log.info("Matrix factorization completed. Generated {} predictions.", predictions.size());
        return predictions;
    }

    private double predictRating(RealMatrix userFeatures, RealMatrix itemFeatures, int userIndex, int itemIndex) {
        double prediction = 0.0;
        for (int f = 0; f < LATENT_FEATURES; f++) {
            prediction += userFeatures.getEntry(userIndex, f) * itemFeatures.getEntry(itemIndex, f);
        }
        return Math.min(5.0, Math.max(1.0, prediction)); // Clip to 1-5 rating scale
    }

    public Map<Long, Map<Long, Double>> buildUserItemMatrix(List<User> users, List<Booking> bookings) {
        Map<Long, Map<Long, Double>> userItemRatings = new HashMap<>();

        // Create a mapping of user IDs to their bookings for faster lookup
        Map<Long, List<Booking>> userBookingsMap = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getUser().getId()));

        for (User user : users) {
            Map<Long, Double> itemRatings = new HashMap<>();
            List<Booking> userBookings = userBookingsMap.get(user.getId());

            if (userBookings != null) {
                for (Booking booking : userBookings) {
                    if (booking.getCar() != null) {
                        double rating = calculateImplicitRating(booking);
                        itemRatings.put(booking.getCar().getId(), rating);
                    }
                }
            }

            userItemRatings.put(user.getId(), itemRatings);
        }

        log.info("Built user-item matrix with {} users and ratings", userItemRatings.size());
        return userItemRatings;
    }

    private double calculateImplicitRating(Booking booking) {
        double baseRating = 3.0; // Base rating for any booking

        // Adjust based on booking duration (longer duration = higher preference)
        if (booking.getEndDate() != null && booking.getStartDate() != null) {
            long days = booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay();
            double durationBonus = Math.min(2.0, days / 7.0); // Max 2 points bonus
            baseRating += durationBonus;
        }

        // Additional factors could be added here:
        // - Repeat bookings
        // - Car category preferences
        // - Seasonal factors
        // - User ratings if available

        return Math.min(5.0, Math.max(1.0, baseRating)); // Ensure rating is between 1-5
    }

    /**
     * Alternative method using matrix operations for prediction
     */
    public RealMatrix predictAllRatings(RealMatrix userFeatures, RealMatrix itemFeatures) {
        return userFeatures.multiply(itemFeatures.transpose());
    }

    /**
     * Get top N recommendations for a user
     */
    public List<Long> getTopRecommendations(Map<Long, Double> predictions, int topN) {
        return predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}