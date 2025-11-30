package com.driverental.onlinecarrental.util;

public class Constants {
    
    // Cache names
    public static final String CACHE_SEARCH_RESULTS = "searchResults";
    public static final String CACHE_INTELLIGENT_SEARCH = "intelligentSearch";
    public static final String CACHE_USER_RECOMMENDATIONS = "userRecommendations";
    public static final String CACHE_VEHICLE_DETAILS = "vehicleDetails";
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    
    // Search
    public static final int MAX_SEARCH_KEYWORDS = 1000;
    public static final int SEARCH_CACHE_TTL_HOURS = 1;
    
    // Recommendations
    public static final int RECOMMENDATION_LIMIT = 10;
    public static final int SIMILAR_USERS_LIMIT = 5;
    
    // Pricing
    public static final double MIN_PRICE_MULTIPLIER = 0.5;
    public static final double MAX_PRICE_MULTIPLIER = 2.0;
    
    // Validation
    public static final int MAX_EMAIL_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_PHONE_LENGTH = 15;
    public static final int MAX_LOCATION_LENGTH = 100;
    public static final int MAX_REVIEW_COMMENT_LENGTH = 500;
    
    private Constants() {
        // Utility class
    }
}