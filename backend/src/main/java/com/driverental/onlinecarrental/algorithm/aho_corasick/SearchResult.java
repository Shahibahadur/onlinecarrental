package com.driverental.onlinecarrental.algorithm.aho_corasick;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * Represents a single match found by the Aho-Corasick algorithm
 * Contains information about the matched keyword and its position in the text
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Comparable<SearchResult> {
    
    /**
     * The keyword that was matched
     */
    private String keyword;
    
    /**
     * The starting index of the match in the text (0-based)
     */
    private int startIndex;
    
    /**
     * The ending index of the match in the text (0-based)
     */
    private int endIndex;
    
    /**
     * The length of the matched keyword
     */
    private int length;
    
    /**
     * Confidence score for the match (0.0 to 1.0)
     * Can be used for ranking multiple matches
     */
    @Builder.Default
    private double confidence = 1.0;
    
    /**
     * Type of match (EXACT, PARTIAL, FUZZY, etc.)
     */
    @Builder.Default
    private MatchType matchType = MatchType.EXACT;
    
    /**
     * Additional metadata about the match
     */
    private String metadata;
    
    /**
     * Constructor with basic parameters
     */
    public SearchResult(String keyword, int startIndex, int endIndex) {
        this.keyword = keyword;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.length = keyword.length();
        this.confidence = 1.0;
        this.matchType = MatchType.EXACT;
    }
    
    /**
     * Constructor with confidence
     */
    public SearchResult(String keyword, int startIndex, int endIndex, double confidence) {
        this(keyword, startIndex, endIndex);
        this.confidence = confidence;
    }
    
    /**
     * Constructor with match type
     */
    public SearchResult(String keyword, int startIndex, int endIndex, MatchType matchType) {
        this(keyword, startIndex, endIndex);
        this.matchType = matchType;
    }
    
    /**
     * Calculate the length of the match
     */
    public int calculateLength() {
        return endIndex - startIndex + 1;
    }
    
    /**
     * Check if this match overlaps with another match
     */
    public boolean overlapsWith(SearchResult other) {
        return this.startIndex <= other.endIndex && other.startIndex <= this.endIndex;
    }
    
    /**
     * Check if this match contains another match
     */
    public boolean contains(SearchResult other) {
        return this.startIndex <= other.startIndex && this.endIndex >= other.endIndex;
    }
    
    /**
     * Check if this match is adjacent to another match
     */
    public boolean isAdjacentTo(SearchResult other) {
        return this.endIndex + 1 == other.startIndex || other.endIndex + 1 == this.startIndex;
    }
    
    /**
     * Merge this match with another overlapping match
     */
    public SearchResult mergeWith(SearchResult other) {
        if (!this.overlapsWith(other) && !this.isAdjacentTo(other)) {
            throw new IllegalArgumentException("Cannot merge non-overlapping or non-adjacent matches");
        }
        
        int newStart = Math.min(this.startIndex, other.startIndex);
        int newEnd = Math.max(this.endIndex, other.endIndex);
        double newConfidence = Math.max(this.confidence, other.confidence);
        
        // Combine keywords (remove duplicates)
        String combinedKeyword = combineKeywords(this.keyword, other.keyword);
        
        return SearchResult.builder()
                .keyword(combinedKeyword)
                .startIndex(newStart)
                .endIndex(newEnd)
                .length(newEnd - newStart + 1)
                .confidence(newConfidence)
                .matchType(determineMergedMatchType(this.matchType, other.matchType))
                .metadata(combineMetadata(this.metadata, other.metadata))
                .build();
    }
    
    /**
     * Get the matched text substring from the original text
     */
    public String getMatchedText(String originalText) {
        if (startIndex < 0 || endIndex >= originalText.length() || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid match indices for the given text");
        }
        return originalText.substring(startIndex, endIndex + 1);
    }
    
    /**
     * Check if this match is exact (confidence = 1.0 and EXACT type)
     */
    public boolean isExactMatch() {
        return confidence == 1.0 && matchType == MatchType.EXACT;
    }
    
    /**
     * Check if this match is partial (confidence < 1.0 or PARTIAL type)
     */
    public boolean isPartialMatch() {
        return confidence < 1.0 || matchType == MatchType.PARTIAL;
    }
    
    /**
     * Calculate relevance score for ranking
     */
    public double calculateRelevanceScore() {
        double baseScore = confidence * 100;
        
        // Adjust score based on match type
        double typeBonus = switch (matchType) {
            case EXACT -> 50.0;
            case PARTIAL -> 25.0;
            case FUZZY -> 10.0;
            case SEMANTIC -> 30.0;
            case PHONETIC -> 15.0;
            case STEMMED -> 12.0;
        };
        
        // Adjust score based on keyword length (longer matches are generally better)
        double lengthBonus = Math.min(length * 2, 20.0);
        
        return baseScore + typeBonus + lengthBonus;
    }
    
    /**
     * Create a copy of this search result with modified properties
     */
    public SearchResult copyWith(String keyword, Integer startIndex, Integer endIndex, 
                               Double confidence, MatchType matchType) {
        return SearchResult.builder()
                .keyword(keyword != null ? keyword : this.keyword)
                .startIndex(startIndex != null ? startIndex : this.startIndex)
                .endIndex(endIndex != null ? endIndex : this.endIndex)
                .length(keyword != null ? keyword.length() : this.length)
                .confidence(confidence != null ? confidence : this.confidence)
                .matchType(matchType != null ? matchType : this.matchType)
                .metadata(this.metadata)
                .build();
    }
    
    /**
     * Convert to a string representation for debugging
     */
    @Override
    public String toString() {
        return String.format("SearchResult{keyword='%s', start=%d, end=%d, length=%d, confidence=%.2f, type=%s}",
                keyword, startIndex, endIndex, length, confidence, matchType);
    }
    
    /**
     * Compare search results by start index for sorting
     */
    @Override
    public int compareTo(SearchResult other) {
        int startCompare = Integer.compare(this.startIndex, other.startIndex);
        if (startCompare != 0) {
            return startCompare;
        }
        
        // If same start index, longer matches come first
        int lengthCompare = Integer.compare(other.length, this.length);
        if (lengthCompare != 0) {
            return lengthCompare;
        }
        
        // If same length, higher confidence comes first
        return Double.compare(other.confidence, this.confidence);
    }
    
    /**
     * Check equality based on content
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SearchResult that = (SearchResult) obj;
        
        if (startIndex != that.startIndex) return false;
        if (endIndex != that.endIndex) return false;
        if (Double.compare(that.confidence, confidence) != 0) return false;
        if (matchType != that.matchType) return false;
        return keyword != null ? keyword.equals(that.keyword) : that.keyword == null;
    }
    
    /**
     * Generate hash code based on content
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = keyword != null ? keyword.hashCode() : 0;
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        temp = Double.doubleToLongBits(confidence);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (matchType != null ? matchType.hashCode() : 0);
        return result;
    }
    
    // Private helper methods
    
    private String combineKeywords(String keyword1, String keyword2) {
        if (keyword1.equals(keyword2)) {
            return keyword1;
        }
        
        // For different keywords, combine them in a meaningful way
        Set<String> uniqueWords = new java.util.HashSet<>();
        java.util.Arrays.stream(keyword1.split("\\s+")).forEach(uniqueWords::add);
        java.util.Arrays.stream(keyword2.split("\\s+")).forEach(uniqueWords::add);
        
        return String.join(" ", uniqueWords);
    }
    
    private MatchType determineMergedMatchType(MatchType type1, MatchType type2) {
        // EXACT is the strongest, then SEMANTIC, then PARTIAL, then FUZZY
        if (type1 == MatchType.EXACT || type2 == MatchType.EXACT) {
            return MatchType.EXACT;
        } else if (type1 == MatchType.SEMANTIC || type2 == MatchType.SEMANTIC) {
            return MatchType.SEMANTIC;
        } else if (type1 == MatchType.PARTIAL || type2 == MatchType.PARTIAL) {
            return MatchType.PARTIAL;
        } else {
            return MatchType.FUZZY;
        }
    }
    
    private String combineMetadata(String metadata1, String metadata2) {
        if (metadata1 == null) return metadata2;
        if (metadata2 == null) return metadata1;
        
        // Simple combination - in real implementation, you might want more sophisticated merging
        return metadata1 + "|" + metadata2;
    }
    
    /**
     * Enum representing different types of matches
     */
    public enum MatchType {
        /**
         * Exact character-by-character match
         */
        EXACT,
        
        /**
         * Partial match (substring match)
         */
        PARTIAL,
        
        /**
         * Fuzzy match with some characters different
         */
        FUZZY,
        
        /**
         * Semantic match (similar meaning)
         */
        SEMANTIC,
        
        /**
         * Phonetic match (sounds similar)
         */
        PHONETIC,
        
        /**
         * Stemmed match (same word root)
         */
        STEMMED
    }
    
    /**
     * Builder class with additional convenience methods
     */
    public static class SearchResultBuilder {
        
        /**
         * Set the length automatically based on start and end indices
         */
        public SearchResultBuilder calculateLength() {
            if (startIndex != 0 && endIndex != 0) {
                this.length = endIndex - startIndex + 1;
            }
            return this;
        }
        
        /**
         * Set match type based on confidence level
         */
        public SearchResultBuilder inferMatchType() {
            if (this.confidence == 1.0) {
                this.matchType = MatchType.EXACT;
            } else if (this.confidence >= 0.8) {
                this.matchType = MatchType.PARTIAL;
            } else if (this.confidence >= 0.6) {
                this.matchType = MatchType.FUZZY;
            } else {
                this.matchType = MatchType.SEMANTIC;
            }
            return this;
        }
        
        /**
         * Build with automatic length calculation
         */
        public SearchResult build() {
            if (this.length == 0 && this.keyword != null) {
                this.length = this.keyword.length();
            }
            if (this.length == 0 && this.startIndex != 0 && this.endIndex != 0) {
                this.length = this.endIndex - this.startIndex + 1;
            }
            return new SearchResult(keyword, startIndex, endIndex, length, this.cconfidence, this.matchType, metadata);
        }
    }
}