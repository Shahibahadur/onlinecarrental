package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.recommendation.CollaborativeFiltering;
import com.driverental.onlinecarrental.algorithm.recommendation.HybridRecommender;
import com.driverental.onlinecarrental.algorithm.recommendation.MatrixFactorization;
import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.enum.VehicleType;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    private final HybridRecommender hybridRecommender;
    private final CollaborativeFiltering collaborativeFiltering;
    private final MatrixFactorization matrixFactorization;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    
    // In-memory cache for user interactions (in production, use Redis)
    private final Map<Long, List<UserInteraction>> userInteractions = new HashMap<>();
    
    @Override
    @Cacheable(value = "userRecommendations", key = "#userId")
    public List<Vehicle> getRecommendationsForUser(Long userId) {
        log.info("Generating personalized recommendations for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if user has sufficient data for personalization
        if (!hasSufficientDataForPersonalization(userId)) {
            log.info("Insufficient data for user {}, returning cold start recommendations", userId);
            return getColdStartRecommendations();
        }
        
        List<User> allUsers = userRepository.findAll();
        List<Vehicle> allVehicles = vehicleRepository.findByIsAvailableTrue();
        
        List<Vehicle> recommendations = hybridRecommender.getHybridRecommendations(
            user, allUsers, allVehicles, 10);
        
        log.info("Generated {} personalized recommendations for user {}", recommendations.size(), userId);
        return recommendations;
    }
    
    @Override
    @Cacheable(value = "popularVehicles", key = "'popular'")
    public List<Vehicle> getPopularVehicles() {
        log.info("Fetching popular vehicles");
        return vehicleRepository.findTop10ByOrderByRatingDescReviewCountDesc();
    }
    
    @Override
    @Cacheable(value = "similarVehicles", key = "#vehicleId")
    public List<Vehicle> getSimilarVehicles(Long vehicleId) {
        log.info("Finding similar vehicles for vehicle: {}", vehicleId);
        
        Vehicle targetVehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
        
        List<Vehicle> allVehicles = vehicleRepository.findByIsAvailableTrue();
        
        return allVehicles.stream()
                .filter(vehicle -> !vehicle.getId().equals(vehicleId))
                .filter(vehicle -> vehicle.getIsAvailable())
                .sorted((v1, v2) -> calculateSimilarityScore(targetVehicle, v1)
                        .compareTo(calculateSimilarityScore(targetVehicle, v2)))
                .limit(5)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getSearchBasedRecommendations(Long userId) {
        log.info("Generating search-based recommendations for user: {}", userId);
        
        // Get user's recent search interactions
        List<UserInteraction> searches = getUserInteractions(userId).stream()
                .filter(interaction -> "SEARCH".equals(interaction.getType()))
                .sorted(Comparator.comparing(UserInteraction::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        if (searches.isEmpty()) {
            return getPopularVehicles();
        }
        
        // Extract keywords from searches and find similar vehicles
        Set<String> searchKeywords = searches.stream()
                .map(UserInteraction::getDetails)
                .collect(Collectors.toSet());
        
        return vehicleRepository.findByIsAvailableTrue().stream()
                .filter(vehicle -> matchesSearchKeywords(vehicle, searchKeywords))
                .sorted(Comparator.comparing(Vehicle::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getFeatureBasedRecommendations(Long userId, List<String> preferredFeatures) {
        log.info("Generating feature-based recommendations for user: {}", userId);
        
        if (preferredFeatures == null || preferredFeatures.isEmpty()) {
            // If no features specified, infer from user's booking history
            preferredFeatures = inferPreferredFeatures(userId);
        }
        
        List<Vehicle> allVehicles = vehicleRepository.findByIsAvailableTrue();
        
        return allVehicles.stream()
                .filter(vehicle -> hasMatchingFeatures(vehicle, preferredFeatures))
                .sorted(Comparator.comparing(Vehicle::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getLocationBasedRecommendations(Long userId, String location) {
        log.info("Generating location-based recommendations for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Use provided location or user's common location from bookings
        String targetLocation = (location != null) ? location : getUserCommonLocation(userId);
        
        return vehicleRepository.findByLocationContainingIgnoreCaseAndIsAvailableTrue(targetLocation, 
                org.springframework.data.domain.PageRequest.of(0, 10))
                .stream()
                .sorted(Comparator.comparing(Vehicle::getRating).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getBudgetBasedRecommendations(Long userId, Double maxDailyPrice) {
        log.info("Generating budget-based recommendations for user: {}", userId);
        
        if (maxDailyPrice == null) {
            // Infer budget from user's booking history
            maxDailyPrice = inferUserBudget(userId);
        }
        
        return vehicleRepository.findByIsAvailableTrue().stream()
                .filter(vehicle -> vehicle.getDailyPrice().doubleValue() <= maxDailyPrice)
                .sorted(Comparator.comparing(Vehicle::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public RecommendationResponse getHybridRecommendations(Long userId) {
        log.info("Generating comprehensive hybrid recommendations for user: {}", userId);
        
        List<Vehicle> personalized = getRecommendationsForUser(userId);
        List<Vehicle> popular = getPopularVehicles();
        List<Vehicle> trending = getTrendingVehicles();
        List<Vehicle> diverse = getDiverseRecommendations(userId, 3);
        
        // Combine and deduplicate recommendations
        Set<Vehicle> allRecommendations = new LinkedHashSet<>();
        allRecommendations.addAll(personalized);
        allRecommendations.addAll(popular);
        allRecommendations.addAll(trending);
        allRecommendations.addAll(diverse);
        
        List<Vehicle> finalRecommendations = new ArrayList<>(allRecommendations)
                .stream()
                .limit(15)
                .collect(Collectors.toList());
        
        return RecommendationResponse.builder()
                .userId(userId)
                .recommendations(finalRecommendations)
                .personalizedCount(personalized.size())
                .popularCount(popular.size())
                .trendingCount(trending.size())
                .diverseCount(diverse.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }
    
    @Override
    public Map<String, Object> getRecommendationExplanations(Long userId, Long vehicleId) {
        log.info("Generating explanation for recommendation: user={}, vehicle={}", userId, vehicleId);
        
        Map<String, Object> explanations = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        
        if (user == null || vehicle == null) {
            return explanations;
        }
        
        // Similar users also booked this
        List<User> similarUsers = findSimilarUsers(userId, 3);
        if (!similarUsers.isEmpty()) {
            explanations.put("similarUsers", 
                similarUsers.stream()
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .collect(Collectors.toList()));
        }
        
        // Features matching user preferences
        List<String> userPreferences = inferPreferredFeatures(userId);
        List<String> matchingFeatures = vehicle.getFeatures().stream()
                .filter(userPreferences::contains)
                .collect(Collectors.toList());
        if (!matchingFeatures.isEmpty()) {
            explanations.put("matchingFeatures", matchingFeatures);
        }
        
        // Price justification
        Double userAvgSpent = getUserAverageSpending(userId);
        if (userAvgSpent != null && vehicle.getDailyPrice().doubleValue() <= userAvgSpent * 1.2) {
            explanations.put("priceReason", "Within your typical budget");
        }
        
        // Location convenience
        String userCommonLocation = getUserCommonLocation(userId);
        if (userCommonLocation != null && vehicle.getLocation().contains(userCommonLocation)) {
            explanations.put("locationReason", "Convenient location based on your history");
        }
        
        // High rating
        if (vehicle.getRating() >= 4.0) {
            explanations.put("ratingReason", "Highly rated by other users");
        }
        
        return explanations;
    }
    
    @Override
    @CacheEvict(value = {"userRecommendations", "popularVehicles", "similarVehicles", "trendingVehicles"}, allEntries = true)
    public void refreshRecommendationModel() {
        log.info("Refreshing recommendation models");
        // In a real implementation, this would retrain ML models
        // For now, we just clear the caches
    }
    
    @Override
    public Map<String, Double> getRecommendationMetrics() {
        log.info("Calculating recommendation metrics");
        
        Map<String, Double> metrics = new HashMap<>();
        
        // Calculate click-through rate (simplified)
        long totalImpressions = userInteractions.values().stream()
                .flatMap(List::stream)
                .filter(i -> "IMPRESSION".equals(i.getType()))
                .count();
        long totalClicks = userInteractions.values().stream()
                .flatMap(List::stream)
                .filter(i -> "CLICK".equals(i.getType()))
                .count();
        
        double ctr = (totalImpressions > 0) ? (double) totalClicks / totalImpressions : 0.0;
        metrics.put("clickThroughRate", ctr);
        
        // Calculate conversion rate
        long totalBookingsFromRecs = userInteractions.values().stream()
                .flatMap(List::stream)
                .filter(i -> "BOOKING".equals(i.getType()))
                .count();
        
        double conversionRate = (totalClicks > 0) ? (double) totalBookingsFromRecs / totalClicks : 0.0;
        metrics.put("conversionRate", conversionRate);
        
        // Calculate diversity score
        metrics.put("diversityScore", calculateDiversityScore());
        
        // Calculate coverage (percentage of users with personalized recommendations)
        long totalUsers = userRepository.count();
        long usersWithData = userRepository.findAll().stream()
                .filter(user -> hasSufficientDataForPersonalization(user.getId()))
                .count();
        
        double coverage = (totalUsers > 0) ? (double) usersWithData / totalUsers : 0.0;
        metrics.put("coverage", coverage);
        
        return metrics;
    }
    
    @Override
    public void trackUserInteraction(Long userId, Long vehicleId, String interactionType) {
        log.debug("Tracking user interaction: user={}, vehicle={}, type={}", userId, vehicleId, interactionType);
        
        UserInteraction interaction = UserInteraction.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .type(interactionType)
                .timestamp(LocalDateTime.now())
                .build();
        
        userInteractions.computeIfAbsent(userId, k -> new ArrayList<>()).add(interaction);
        
        // Keep only recent interactions (last 100 per user)
        List<UserInteraction> userInteractionsList = userInteractions.get(userId);
        if (userInteractionsList.size() > 100) {
            userInteractionsList = userInteractionsList.subList(
                userInteractionsList.size() - 100, userInteractionsList.size());
            userInteractions.put(userId, userInteractionsList);
        }
    }
    
    @Override
    @Cacheable(value = "trendingVehicles", key = "'trending'")
    public List<Vehicle> getTrendingVehicles() {
        log.info("Fetching trending vehicles");
        
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        
        // Get vehicles with recent bookings and high engagement
        return vehicleRepository.findByIsAvailableTrue().stream()
                .sorted((v1, v2) -> {
                    double score1 = calculateTrendingScore(v1, oneWeekAgo);
                    double score2 = calculateTrendingScore(v2, oneWeekAgo);
                    return Double.compare(score2, score1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getColdStartRecommendations() {
        log.info("Generating cold start recommendations for new users");
        
        // Combine popular, highly-rated, and diverse vehicles
        List<Vehicle> popular = getPopularVehicles();
        List<Vehicle> highlyRated = vehicleRepository.findByIsAvailableTrue().stream()
                .filter(v -> v.getRating() >= 4.5)
                .sorted(Comparator.comparing(Vehicle::getReviewCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        Set<Vehicle> recommendations = new LinkedHashSet<>();
        recommendations.addAll(popular);
        recommendations.addAll(highlyRated);
        
        return new ArrayList<>(recommendations).stream()
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getDiverseRecommendations(Long userId, Integer diversityFactor) {
        log.info("Generating diverse recommendations for user: {}", userId);
        
        int factor = (diversityFactor != null) ? diversityFactor : 3;
        List<Vehicle> baseRecommendations = getRecommendationsForUser(userId);
        
        if (baseRecommendations.isEmpty()) {
            return getColdStartRecommendations();
        }
        
        // Group by vehicle type and select top from each group
        Map<VehicleType, List<Vehicle>> byType = baseRecommendations.stream()
                .collect(Collectors.groupingBy(Vehicle::getType));
        
        List<Vehicle> diverse = new ArrayList<>();
        for (List<Vehicle> vehicles : byType.values()) {
            diverse.addAll(vehicles.stream().limit(factor).collect(Collectors.toList()));
        }
        
        // Add some random vehicles for extra diversity
        List<Vehicle> allVehicles = vehicleRepository.findByIsAvailableTrue();
        Collections.shuffle(allVehicles);
        diverse.addAll(allVehicles.stream()
                .filter(v -> !baseRecommendations.contains(v))
                .limit(2)
                .collect(Collectors.toList()));
        
        return diverse.stream().distinct().limit(10).collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> getSeasonalRecommendations() {
        log.info("Generating seasonal recommendations");
        
        Month currentMonth = LocalDate.now().getMonth();
        VehicleType seasonalType = getSeasonalVehicleType(currentMonth);
        
        return vehicleRepository.findByIsAvailableTrue().stream()
                .filter(vehicle -> vehicle.getType() == seasonalType)
                .sorted(Comparator.comparing(Vehicle::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    public Boolean hasSufficientDataForPersonalization(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        // Check if user has at least 2 bookings or 5 interactions
        long bookingCount = user.getBookings().size();
        long interactionCount = getUserInteractions(userId).size();
        
        return bookingCount >= 2 || interactionCount >= 5;
    }
    
    // Helper methods
    
    private Double calculateSimilarityScore(Vehicle v1, Vehicle v2) {
        double score = 0.0;
        
        // Type similarity
        if (v1.getType().equals(v2.getType())) {
            score += 0.4;
        }
        
        // Price similarity (within 20%)
        double priceDiff = Math.abs(v1.getDailyPrice().doubleValue() - v2.getDailyPrice().doubleValue());
        double avgPrice = (v1.getDailyPrice().doubleValue() + v2.getDailyPrice().doubleValue()) / 2;
        if (priceDiff / avgPrice <= 0.2) {
            score += 0.3;
        }
        
        // Feature similarity
        Set<String> features1 = new HashSet<>(v1.getFeatures());
        Set<String> features2 = new HashSet<>(v2.getFeatures());
        features1.retainAll(features2);
        double featureSimilarity = (double) features1.size() / 
                                 (features1.size() + features2.size() - features1.size());
        score += featureSimilarity * 0.3;
        
        return score;
    }
    
    private boolean matchesSearchKeywords(Vehicle vehicle, Set<String> keywords) {
        String vehicleText = (vehicle.getMake() + " " + vehicle.getModel() + " " + 
                            vehicle.getType() + " " + String.join(" ", vehicle.getFeatures()))
                            .toLowerCase();
        
        return keywords.stream()
                .anyMatch(keyword -> vehicleText.contains(keyword.toLowerCase()));
    }
    
    private List<String> inferPreferredFeatures(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Arrays.asList("Air Conditioning", "Bluetooth", "GPS");
        }
        
        // Extract features from user's booked vehicles
        return user.getBookings().stream()
                .map(Booking::getVehicle)
                .flatMap(vehicle -> vehicle.getFeatures().stream())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }
    
    private boolean hasMatchingFeatures(Vehicle vehicle, List<String> preferredFeatures) {
        return vehicle.getFeatures().stream()
                .anyMatch(preferredFeatures::contains);
    }
    
    private String getUserCommonLocation(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        return user.getBookings().stream()
                .map(Booking::getPickupLocation)
                .collect(Collectors.groupingBy(l -> l, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    private Double inferUserBudget(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getBookings().isEmpty()) {
            return 100.0; // Default budget
        }
        
        return user.getBookings().stream()
                .mapToDouble(booking -> booking.getTotalPrice().doubleValue() / 
                    (booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay()))
                .average()
                .orElse(100.0);
    }
    
    private List<User> findSimilarUsers(Long userId, int limit) {
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return Collections.emptyList();
        }
        
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(userId))
                .sorted((u1, u2) -> Double.compare(
                    calculateUserSimilarity(targetUser, u2),
                    calculateUserSimilarity(targetUser, u1)
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    private Double calculateUserSimilarity(User u1, User u2) {
        // Simple similarity based on booked vehicle types
        Set<VehicleType> types1 = u1.getBookings().stream()
                .map(booking -> booking.getVehicle().getType())
                .collect(Collectors.toSet());
        Set<VehicleType> types2 = u2.getBookings().stream()
                .map(booking -> booking.getVehicle().getType())
                .collect(Collectors.toSet());
        
        Set<VehicleType> intersection = new HashSet<>(types1);
        intersection.retainAll(types2);
        Set<VehicleType> union = new HashSet<>(types1);
        union.addAll(types2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private Double getUserAverageSpending(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getBookings().isEmpty()) {
            return null;
        }
        
        return user.getBookings().stream()
                .mapToDouble(booking -> booking.getTotalPrice().doubleValue() / 
                    (booking.getEndDate().toEpochDay() - booking.getStartDate().toEpochDay()))
                .average()
                .orElse(null);
    }
    
    private Double calculateTrendingScore(Vehicle vehicle, LocalDate sinceDate) {
        long recentBookings = bookingRepository.countByVehicleIdAndStartDateAfter(vehicle.getId(), sinceDate);
        long recentInteractions = getUserInteractionsForVehicle(vehicle.getId(), sinceDate);
        
        return (recentBookings * 2.0) + (recentInteractions * 0.5) + (vehicle.getRating() * 10);
    }
    
    private long getUserInteractionsForVehicle(Long vehicleId, LocalDate sinceDate) {
        return userInteractions.values().stream()
                .flatMap(List::stream)
                .filter(interaction -> interaction.getVehicleId().equals(vehicleId))
                .filter(interaction -> interaction.getTimestamp().toLocalDate().isAfter(sinceDate))
                .count();
    }
    
    private List<UserInteraction> getUserInteractions(Long userId) {
        return userInteractions.getOrDefault(userId, Collections.emptyList());
    }
    
    private Double calculateDiversityScore() {
        // Calculate how diverse recommendations are across different vehicle types
        List<Vehicle> sampleRecommendations = getPopularVehicles();
        long distinctTypes = sampleRecommendations.stream()
                .map(Vehicle::getType)
                .distinct()
                .count();
        
        return (double) distinctTypes / VehicleType.values().length;
    }
    
    private VehicleType getSeasonalVehicleType(Month month) {
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> VehicleType.SUV; // Winter - SUVs for snow
            case JUNE, JULY, AUGUST -> VehicleType.CONVERTIBLE; // Summer - Convertibles
            case MARCH, APRIL, MAY, SEPTEMBER, OCTOBER, NOVEMBER -> VehicleType.SEDAN; // Other seasons
        };
    }
    
    @Scheduled(cron = "0 0 3 * * ?") // Run daily at 3 AM
    public void cleanupOldInteractions() {
        log.info("Cleaning up old user interactions");
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(3);
        
        userInteractions.forEach((userId, interactions) -> {
            List<UserInteraction> recent = interactions.stream()
                    .filter(interaction -> interaction.getTimestamp().isAfter(cutoff))
                    .collect(Collectors.toList());
            userInteractions.put(userId, recent);
        });
    }
    
    // Inner class for tracking user interactions
    @Data
    @Builder
    private static class UserInteraction {
        private Long userId;
        private Long vehicleId;
        private String type; // CLICK, IMPRESSION, BOOKING, SEARCH, etc.
        private String details;
        private LocalDateTime timestamp;
    }
}