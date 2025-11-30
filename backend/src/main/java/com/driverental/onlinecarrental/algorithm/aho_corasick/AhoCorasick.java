package com.driverental.onlinecarrental.algorithm.aho_corasick;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AhoCorasick {
    private TrieNode root;
    private boolean built;
    private Set<String> keywords;

    public AhoCorasick() {
        this.root = new TrieNode();
        this.built = false;
        this.keywords = new HashSet<>();
    }

    /**
     * Build the trie with keywords
     */
    public void buildTrie(List<String> keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
        
        // Build initial trie
        for (String keyword : keywords) {
            addKeyword(keyword.toLowerCase());
        }
        buildFailureLinks();
        built = true;
    }

    /**
     * Search for all keywords in the text with enhanced results
     */
    public List<SearchResult> search(String text) {
        return search(text, SearchConfig.defaultConfig());
    }

    /**
     * Search with custom configuration
     */
    public List<SearchResult> search(String text, SearchConfig config) {
        if (!built) {
            throw new IllegalStateException("Trie must be built before searching");
        }

        List<SearchResult> results = new ArrayList<>();
        TrieNode current = root;
        String searchText = config.isCaseSensitive() ? text : text.toLowerCase();
        
        for (int i = 0; i < searchText.length(); i++) {
            char ch = searchText.charAt(i);
            
            while (current != null && !current.getChildren().containsKey(ch)) {
                current = current.getFailure();
            }
            
            if (current == null) {
                current = root;
                continue;
            }
            
            current = current.getChildren().get(ch);
            
            // Check for matches at current position
            if (current.getOutput() != null) {
                String[] matchedKeywords = current.getOutput().split(",");
                for (String keyword : matchedKeywords) {
                    SearchResult result = createSearchResult(keyword, i, config);
                    if (meetsThreshold(result, config)) {
                        results.add(result);
                    }
                }
            }
            
            // Check for partial matches if enabled
            if (config.isAllowPartialMatches()) {
                List<SearchResult> partialMatches = findPartialMatches(current, i, config);
                results.addAll(partialMatches);
            }
        }
        
        return processResults(results, config);
    }

    /**
     * Search with multiple texts and aggregate results
     */
    public Map<String, List<SearchResult>> searchMultiple(List<String> texts, SearchConfig config) {
        return texts.stream()
                .collect(Collectors.toMap(
                    text -> text,
                    text -> search(text, config)
                ));
    }

    /**
     * Get statistics about the search index
     */
    public Map<String, Object> getSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalKeywords", keywords.size());
        stats.put("trieSize", calculateTrieSize(root));
        stats.put("isBuilt", built);
        stats.put("averageKeywordLength", calculateAverageKeywordLength());
        return stats;
    }

    /**
     * Find similar keywords using fuzzy matching
     */
    public List<SearchResult> findSimilarKeywords(String query, double similarityThreshold) {
        List<SearchResult> similar = new ArrayList<>();
        
        for (String keyword : keywords) {
            double similarity = calculateSimilarity(query, keyword);
            if (similarity >= similarityThreshold) {
                similar.add(SearchResult.builder()
                        .keyword(keyword)
                        .startIndex(0)
                        .endIndex(keyword.length() - 1)
                        .confidence(similarity)
                        .matchType(SearchResult.MatchType.FUZZY)
                        .metadata("similarity: " + similarity)
                        .build());
            }
        }
        
        return similar.stream()
                .sorted(Comparator.comparing(SearchResult::getConfidence).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Clear the trie and reset state
     */
    public void clear() {
        this.root = new TrieNode();
        this.built = false;
        this.keywords.clear();
    }

    // Private implementation methods

    private void addKeyword(String keyword) {
        TrieNode current = root;
        for (char ch : keyword.toCharArray()) {
            current = current.getChildren().computeIfAbsent(ch, c -> new TrieNode());
        }
        current.setOutput(keyword);
    }

    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // First level failure links point to root
        for (TrieNode child : root.getChildren().values()) {
            child.setFailure(root);
            queue.add(child);
        }

        // Build failure links for other nodes
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : current.getChildren().entrySet()) {
                char ch = entry.getKey();
                TrieNode child = entry.getValue();
                
                TrieNode failure = current.getFailure();
                while (failure != null && !failure.getChildren().containsKey(ch)) {
                    failure = failure.getFailure();
                }
                
                if (failure == null) {
                    child.setFailure(root);
                } else {
                    child.setFailure(failure.getChildren().get(ch));
                    // Add outputs from failure node
                    if (child.getFailure().getOutput() != null) {
                        child.addOutput(child.getFailure().getOutput());
                    }
                }
                queue.add(child);
            }
        }
    }

    private SearchResult createSearchResult(String keyword, int endIndex, SearchConfig config) {
        int startIndex = endIndex - keyword.length() + 1;
        double confidence = calculateMatchConfidence(keyword, startIndex, endIndex, config);
        
        SearchResult.MatchType matchType = determineMatchType(keyword, confidence);
        
        return SearchResult.builder()
                .keyword(keyword)
                .startIndex(startIndex)
                .endIndex(endIndex)
                .confidence(confidence)
                .matchType(matchType)
                .metadata(createMetadata(keyword))
                .build();
    }

    private double calculateMatchConfidence(String keyword, int startIndex, int endIndex, SearchConfig config) {
        double baseConfidence = 1.0;
        
        // Adjust confidence based on match characteristics
        if (config.isBoostExactMatches() && isExactContextMatch(keyword, startIndex, endIndex)) {
            baseConfidence *= 1.2;
        }
        
        // Longer matches get slightly higher confidence
        if (keyword.length() > 5) {
            baseConfidence *= 1.1;
        }
        
        return Math.min(baseConfidence, 1.0);
    }

    private SearchResult.MatchType determineMatchType(String keyword, double confidence) {
        if (confidence == 1.0) {
            return SearchResult.MatchType.EXACT;
        } else if (confidence >= 0.8) {
            return SearchResult.MatchType.PARTIAL;
        } else {
            return SearchResult.MatchType.FUZZY;
        }
    }

    private List<SearchResult> findPartialMatches(TrieNode node, int position, SearchConfig config) {
        List<SearchResult> partials = new ArrayList<>();
        
        // Traverse failure links to find partial matches
        TrieNode temp = node;
        while (temp != null && temp != root) {
            if (temp.getOutput() != null) {
                String keyword = temp.getOutput().split(",")[0];
                int matchLength = keyword.length();
                int startIndex = position - matchLength + 1;
                
                if (startIndex >= 0) {
                    SearchResult result = SearchResult.builder()
                            .keyword(keyword)
                            .startIndex(startIndex)
                            .endIndex(position)
                            .confidence(0.7) // Lower confidence for partial matches
                            .matchType(SearchResult.MatchType.PARTIAL)
                            .build();
                    
                    if (meetsThreshold(result, config)) {
                        partials.add(result);
                    }
                }
            }
            temp = temp.getFailure();
        }
        
        return partials;
    }

    private List<SearchResult> processResults(List<SearchResult> results, SearchConfig config) {
        // Remove duplicates
        Set<SearchResult> uniqueResults = new HashSet<>(results);
        
        // Apply confidence threshold
        List<SearchResult> filtered = uniqueResults.stream()
                .filter(result -> result.getConfidence() >= config.getMinConfidence())
                .collect(Collectors.toList());
        
        // Sort by relevance
        filtered.sort(Comparator.comparing(SearchResult::calculateRelevanceScore).reversed());
        
        // Apply result limit
        if (config.getMaxResults() > 0 && filtered.size() > config.getMaxResults()) {
            filtered = filtered.subList(0, config.getMaxResults());
        }
        
        return filtered;
    }

    private boolean meetsThreshold(SearchResult result, SearchConfig config) {
        return result.getConfidence() >= config.getMinConfidence() &&
               result.getLength() >= config.getMinMatchLength();
    }

    private boolean isExactContextMatch(String keyword, int startIndex, int endIndex) {
        // Check if the match is at word boundaries
        // This is a simplified implementation
        return true;
    }

    private String createMetadata(String keyword) {
        return String.format("keyword_length:%d,is_common:%b", 
                keyword.length(), isCommonKeyword(keyword));
    }

    private boolean isCommonKeyword(String keyword) {
        // Simple common keyword detection
        Set<String> commonWords = Set.of("the", "and", "or", "with", "for", "car", "auto");
        return commonWords.contains(keyword.toLowerCase());
    }

    private double calculateSimilarity(String str1, String str2) {
        // Simple Levenshtein distance-based similarity
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = calculateLevenshteinDistance(str1, str2);
        return 1.0 - (double) distance / maxLength;
    }

    private int calculateLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                        dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1),
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    );
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }

    private int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    private int calculateTrieSize(TrieNode node) {
        int size = 1; // Count current node
        for (TrieNode child : node.getChildren().values()) {
            size += calculateTrieSize(child);
        }
        return size;
    }

    private double calculateAverageKeywordLength() {
        return keywords.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0);
    }
}