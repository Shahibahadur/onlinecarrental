package com.driverental.onlinecarrental.algorithm.aho_corasick;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AhoCorasick {
    private TrieNode root;
    private boolean built;

    public AhoCorasick() {
        this.root = new TrieNode();
        this.built = false;
    }

    public void buildTrie(List<String> keywords) {
        // Build initial trie
        for (String keyword : keywords) {
            addKeyword(keyword.toLowerCase());
        }
        buildFailureLinks();
        built = true;
    }

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

    public List<SearchResult> search(String text) {
        if (!built) {
            throw new IllegalStateException("Trie must be built before searching");
        }

        List<SearchResult> results = new ArrayList<>();
        TrieNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toLowerCase(text.charAt(i));
            
            while (current != null && !current.getChildren().containsKey(ch)) {
                current = current.getFailure();
            }
            
            if (current == null) {
                current = root;
                continue;
            }
            
            current = current.getChildren().get(ch);
            
            if (current.getOutput() != null) {
                results.add(new SearchResult(current.getOutput(), i - current.getOutput().length() + 1, i));
            }
        }
        
        return results;
    }

    public void clear() {
        this.root = new TrieNode();
        this.built = false;
    }
}