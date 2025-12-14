package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.recommendation.CollaborativeFiltering;
import com.driverental.onlinecarrental.algorithm.recommendation.HybridRecommender;
import com.driverental.onlinecarrental.algorithm.recommendation.MatrixFactorization;
import com.driverental.onlinecarrental.model.dto.response.RecommendationResponse;
import com.driverental.onlinecarrental.model.entity.Booking;
import com.driverental.onlinecarrental.model.entity.User;
import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.model.enums.CarCategory;
import com.driverental.onlinecarrental.repository.BookingRepository;
import com.driverental.onlinecarrental.repository.UserRepository;
import com.driverental.onlinecarrental.repository.CarRepository;
import com.driverental.onlinecarrental.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;

    // In-memory cache for user interactions (in production, use Redis)
    private final Map<Long, List<UserInteraction>> userInteractions = new HashMap<>();

    @Override
    @Cacheable(value = "userRecommendations", key = "#userId")
    public List<Car> getRecommendationsForUser(Long userId) {
        log.info("Generating personalized recommendations for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if user has sufficient data for personalization
        if (!hasSufficientDataForPersonalization(userId)) {
            log.info("Insufficient data for user {}, returning cold start recommendations", userId);
            return getColdStartRecommendations();
        }

        List<User> allUsers = userRepository.findAll();
        List<Car> allCars = carRepository.findByIsAvailableTrue();

        List<Car> recommendations = hybridRecommender.getHybridRecommendations(
                user, allUsers, allCars, 10);

        log.info("Generated {} personalized recommendations for user {}", recommendations.size(), userId);
        return recommendations;
    }

    @Override
    @Cacheable(value = "popularCars", key = "'popular'")
    public List<Car> getPopularCars() {
        log.info("Fetching popular cars");
        return carRepository.findTop10ByOrderByRatingDescReviewCountDesc();
    }

    @Override
    @Cacheable(value = "similarCars", key = "#carId")
    public List<Car> getSimilarCars(Long carId) {
        log.info("Finding similar cars for car: {}", carId);

        Car targetCar = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + carId));

        List<Car> allCars = carRepository.findByIsAvailableTrue();

        return allCars.stream()
                .filter(car -> !car.getId().equals(carId))
                .filter(car -> car.getIsAvailable())
                .sorted((v1, v2) -> calculateSimilarityScore(targetCar, v1)
                        .compareTo(calculateSimilarityScore(targetCar, v2)))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getSearchBasedRecommendations(Long userId) {
        log.info("Generating search-based recommendations for user: {}", userId);

        // Get user's recent search interactions
        List<UserInteraction> searches = getUserInteractions(userId).stream()
                .filter(interaction -> "SEARCH".equals(interaction.getType()))
                .sorted(Comparator.comparing(UserInteraction::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (searches.isEmpty()) {
            return getPopularCars();
        }

        // Extract keywords from searches and find similar cars
        Set<String> searchKeywords = searches.stream()
                .map(UserInteraction::getDetails)
                .collect(Collectors.toSet());

        return carRepository.findByIsAvailableTrue().stream()
                .filter(car -> matchesSearchKeywords(car, searchKeywords))
                .sorted(Comparator.comparing(Car::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getFeatureBasedRecommendations(Long userId, List<String> preferredFeatures) {
        log.info("Generating feature-based recommendations for user: {}", userId);

        List<String> effectiveFeatures;
        if (preferredFeatures == null || preferredFeatures.isEmpty()) {
            // If no features specified, infer from user's booking history
            effectiveFeatures = inferPreferredFeatures(userId);
        } else {
            effectiveFeatures = preferredFeatures;
        }

        List<Car> allCars = carRepository.findByIsAvailableTrue();

        return allCars.stream()
                .filter(car -> hasMatchingFeatures(car, effectiveFeatures))
                .sorted(Comparator.comparing(Car::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getLocationBasedRecommendations(Long userId, String location) {
        log.info("Generating location-based recommendations for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use provided location or user's common location from bookings
        String targetLocation = (location != null) ? location : getUserCommonLocation(userId);

        return carRepository.findByLocationContainingIgnoreCaseAndIsAvailableTrue(targetLocation,
                org.springframework.data.domain.PageRequest.of(0, 10))
                .stream()
                .sorted(Comparator.comparing(Car::getRating).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getBudgetBasedRecommendations(Long userId, Double maxDailyPrice) {
        log.info("Generating budget-based recommendations for user: {}", userId);

        Double effectiveMaxPrice;
        if (maxDailyPrice == null) {
            // Infer budget from user's booking history
            effectiveMaxPrice = inferUserBudget(userId);
        } else {
            effectiveMaxPrice = maxDailyPrice;
        }

        return carRepository.findByIsAvailableTrue().stream()
                .filter(car -> car.getDailyPrice().doubleValue() <= effectiveMaxPrice)
                .sorted(Comparator.comparing(Car::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public RecommendationResponse getHybridRecommendations(Long userId) {
        log.info("Generating comprehensive hybrid recommendations for user: {}", userId);

        List<Car> personalized = getRecommendationsForUser(userId);
        List<Car> popular = getPopularCars();
        List<Car> trending = getTrendingCars();
        List<Car> diverse = getDiverseRecommendations(userId, 3);

        // Combine and deduplicate recommendations
        Set<Car> allRecommendations = new LinkedHashSet<>();
        allRecommendations.addAll(personalized);
        allRecommendations.addAll(popular);
        allRecommendations.addAll(trending);
        allRecommendations.addAll(diverse);

        List<Car> finalRecommendations = new ArrayList<>(allRecommendations)
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
    public Map<String, Object> getRecommendationExplanations(Long userId, Long carId) {
        log.info("Generating explanation for recommendation: user={}, car={}", userId, carId);

        Map<String, Object> explanations = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        Car car = carRepository.findById(carId).orElse(null);

        if (user == null || car == null) {
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
        List<String> matchingFeatures = car.getFeatures().stream()
                .filter(userPreferences::contains)
                .collect(Collectors.toList());
        if (!matchingFeatures.isEmpty()) {
            explanations.put("matchingFeatures", matchingFeatures);
        }

        // Price justification
        Double userAvgSpent = getUserAverageSpending(userId);
        if (userAvgSpent != null && car.getDailyPrice().doubleValue() <= userAvgSpent * 1.2) {
            explanations.put("priceReason", "Within your typical budget");
        }

        // Location convenience
        String userCommonLocation = getUserCommonLocation(userId);
        if (userCommonLocation != null && car.getLocation().contains(userCommonLocation)) {
            explanations.put("locationReason", "Convenient location based on your history");
        }

        // High rating
        if (car.getRating() >= 4.0) {
            explanations.put("ratingReason", "Highly rated by other users");
        }

        return explanations;
    }

    @Override
    @CacheEvict(value = { "userRecommendations", "popularCars", "similarCars",
            "trendingCars" }, allEntries = true)
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
    public void trackUserInteraction(Long userId, Long carId, String interactionType) {
        log.debug("Tracking user interaction: user={}, car={}, type={}", userId, carId, interactionType);

        UserInteraction interaction = UserInteraction.builder()
                .userId(userId)
                .carId(carId)
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
    @Cacheable(value = "trendingCars", key = "'trending'")
    public List<Car> getTrendingCars() {
        log.info("Fetching trending cars");

        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);

        // Get cars with recent bookings and high engagement
        return carRepository.findByIsAvailableTrue().stream()
                .sorted((v1, v2) -> {
                    double score1 = calculateTrendingScore(v1, oneWeekAgo);
                    double score2 = calculateTrendingScore(v2, oneWeekAgo);
                    return Double.compare(score2, score1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getColdStartRecommendations() {
        log.info("Generating cold start recommendations for new users");

        // Combine popular, highly-rated, and diverse cars
        List<Car> popular = getPopularCars();
        List<Car> highlyRated = carRepository.findByIsAvailableTrue().stream()
                .filter(v -> v.getRating() >= 4.5)
                .sorted(Comparator.comparing(Car::getReviewCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Set<Car> recommendations = new LinkedHashSet<>();
        recommendations.addAll(popular);
        recommendations.addAll(highlyRated);

        return new ArrayList<>(recommendations).stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Car> getDiverseRecommendations(Long userId, Integer diversityFactor) {
        log.info("Generating diverse recommendations for user: {}", userId);

        int factor = (diversityFactor != null) ? diversityFactor : 3;
        List<Car> baseRecommendations = getRecommendationsForUser(userId);

        if (baseRecommendations.isEmpty()) {
            return getColdStartRecommendations();
        }

        // Group by car type and select top from each group
        Map<CarCategory, List<Car>> byType = baseRecommendations.stream()
                .collect(Collectors.groupingBy(Car::getType));

        List<Car> diverse = new ArrayList<>();
        for (List<Car> cars : byType.values()) {
            diverse.addAll(cars.stream().limit(factor).collect(Collectors.toList()));
        }

        // Add some random cars for extra diversity
        List<Car> allCars = carRepository.findByIsAvailableTrue();
        Collections.shuffle(allCars);
        diverse.addAll(allCars.stream()
                .filter(v -> !baseRecommendations.contains(v))
                .limit(2)
                .collect(Collectors.toList()));

        return diverse.stream().distinct().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<Car> getSeasonalRecommendations() {
        log.info("Generating seasonal recommendations");

        Month currentMonth = LocalDate.now().getMonth();
        CarCategory seasonalType = getSeasonalCarCategory(currentMonth);

        return carRepository.findByIsAvailableTrue().stream()
                .filter(car -> car.getType() == seasonalType)
                .sorted(Comparator.comparing(Car::getRating).reversed())
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

    private Double calculateSimilarityScore(Car v1, Car v2) {
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

    private boolean matchesSearchKeywords(Car car, Set<String> keywords) {
        String carText = (car.getMake() + " " + car.getModel() + " " +
                car.getType() + " " + String.join(" ", car.getFeatures()))
                .toLowerCase();

        return keywords.stream()
                .anyMatch(keyword -> carText.contains(keyword.toLowerCase()));
    }

    private List<String> inferPreferredFeatures(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Arrays.asList("Air Conditioning", "Bluetooth", "GPS");
        }

        // Extract features from user's booked cars
        return user.getBookings().stream()
                .map(Booking::getCar)
                .flatMap(car -> car.getFeatures().stream())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean hasMatchingFeatures(Car car, List<String> preferredFeatures) {
        return car.getFeatures().stream()
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
                        calculateUserSimilarity(targetUser, u1)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Double calculateUserSimilarity(User u1, User u2) {
        // Simple similarity based on booked car types
        Set<CarCategory> types1 = u1.getBookings().stream()
                .map(booking -> booking.getCar().getType())
                .collect(Collectors.toSet());
        Set<CarCategory> types2 = u2.getBookings().stream()
                .map(booking -> booking.getCar().getType())
                .collect(Collectors.toSet());

        Set<CarCategory> intersection = new HashSet<>(types1);
        intersection.retainAll(types2);
        Set<CarCategory> union = new HashSet<>(types1);
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
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private Double calculateTrendingScore(Car car, LocalDate sinceDate) {
        long recentBookings = bookingRepository.countByCarIdAndStartDateAfter(car.getId(), sinceDate);
        long recentInteractions = getUserInteractionsForCar(car.getId(), sinceDate);

        return (recentBookings * 2.0) + (recentInteractions * 0.5) + (car.getRating() * 10);
    }

    private long getUserInteractionsForCar(Long carId, LocalDate sinceDate) {
        return userInteractions.values().stream()
                .flatMap(List::stream)
                .filter(interaction -> interaction.getCarId().equals(carId))
                .filter(interaction -> interaction.getTimestamp().toLocalDate().isAfter(sinceDate))
                .count();
    }

    private List<UserInteraction> getUserInteractions(Long userId) {
        return userInteractions.getOrDefault(userId, Collections.emptyList());
    }

    private Double calculateDiversityScore() {
        // Calculate how diverse recommendations are across different car types
        List<Car> sampleRecommendations = getPopularCars();
        long distinctTypes = sampleRecommendations.stream()
                .map(Car::getType)
                .distinct()
                .count();

        return (double) distinctTypes / CarCategory.values().length;
    }

    private CarCategory getSeasonalCarCategory(Month month) {
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> CarCategory.SUV; // Winter - SUVs for snow
            case JUNE, JULY, AUGUST -> CarCategory.CONVERTIBLE; // Summer - Convertibles
            case MARCH, APRIL, MAY, SEPTEMBER, OCTOBER, NOVEMBER -> CarCategory.SEDAN; // Other seasons
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
        private Long carId;
        private String type; // CLICK, IMPRESSION, BOOKING, SEARCH, etc.
        private String details;
        private LocalDateTime timestamp;
    }
}