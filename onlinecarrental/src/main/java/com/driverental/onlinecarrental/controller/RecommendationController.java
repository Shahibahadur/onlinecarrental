package com.driverental.onlinecarrental.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @GetMapping
    public ResponseEntity<String> recommend() {
        return ResponseEntity.ok("Recommendation controller ready");
    }
}
