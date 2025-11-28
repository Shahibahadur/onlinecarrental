package com.driverental.onlinecarrental.algorithm.aho_corasick;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for customizing search behavior
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchConfig {
    
    /**
     * Minimum confidence threshold for matches (0.0 to 1.0)
     */
    @Builder.Default
    private double minConfidence = 0.5;
    
    /**
     * Minimum length for a valid match
     */
    @Builder.Default
    private int minMatchLength = 2;
    
    /**
     * Maximum number of results to return (0 for unlimited)
     */
    @Builder.Default
    private int maxResults = 100;
    
    /**
     * Whether search should be case-sensitive
     */
    @Builder.Default
    private boolean caseSensitive = false;
    
    /**
     * Whether to allow partial matches
     */
    @Builder.Default
    private boolean allowPartialMatches = true;
    
    /**
     * Whether to boost exact matches
     */
    @Builder.Default
    private boolean boostExactMatches = true;
    
    /**
     * Whether to remove overlapping matches
     */
    @Builder.Default
    private boolean removeOverlaps = true;
    
    /**
     * Whether to include match metadata
     */
    @Builder.Default
    private boolean includeMetadata = true;
    
    /**
     * Default configuration with standard settings
     */
    public static SearchConfig defaultConfig() {
        return SearchConfig.builder().build();
    }
    
    /**
     * Strict configuration for high-precision searches
     */
    public static SearchConfig strictConfig() {
        return SearchConfig.builder()
                .minConfidence(0.8)
                .minMatchLength(3)
                .allowPartialMatches(false)
                .boostExactMatches(true)
                .build();
    }
    
    /**
     * Lenient configuration for high-recall searches
     */
    public static SearchConfig lenientConfig() {
        return SearchConfig.builder()
                .minConfidence(0.3)
                .minMatchLength(1)
                .allowPartialMatches(true)
                .boostExactMatches(false)
                .maxResults(200)
                .build();
    }
    
    /**
     * Fast configuration for performance-critical searches
     */
    public static SearchConfig fastConfig() {
        return SearchConfig.builder()
                .minConfidence(0.7)
                .maxResults(50)
                .allowPartialMatches(false)
                .removeOverlaps(false)
                .includeMetadata(false)
                .build();
    }
}