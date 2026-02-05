package com.driverental.onlinecarrental.model.dto.response;

import java.util.Map;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private Long userId;
    private List<Vehicle> recommendations;
    private Integer personalizedCount;
    private Integer popularCount;
    private Integer trendingCount;
    private Integer diverseCount;
    private String recommendationStrategy;
    private LocalDateTime generatedAt;
    private Map<String, Object> metadata;

    // Helper methods
    public Integer getTotalRecommendations() {
        return recommendations != null ? recommendations.size() : 0;
    }

    public String getSummary() {
        return String.format("Generated %d recommendations using hybrid approach " +
                "(%d personalized, %d popular, %d trending, %d diverse)",
                getTotalRecommendations(), personalizedCount, popularCount,
                trendingCount, diverseCount);
    }
}