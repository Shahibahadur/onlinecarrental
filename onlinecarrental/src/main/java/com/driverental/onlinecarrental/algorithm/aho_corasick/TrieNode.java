package com.driverental.onlinecarrental.algorithm.aho_corasick;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class TrieNode {
    private Map<Character, TrieNode> children;
    private TrieNode failure;
    private String output;
    
    public TrieNode() {
        this.children = new HashMap<>();
        this.failure = null;
        this.output = null;
    }
    
    public void addOutput(String output) {
        if (this.output == null) {
            this.output = output;
        } else {
            this.output += "," + output;
        }
    }
}