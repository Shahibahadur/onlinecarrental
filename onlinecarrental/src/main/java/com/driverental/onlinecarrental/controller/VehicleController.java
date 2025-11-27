package com.driverental.onlinecarrental.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @GetMapping
    public ResponseEntity<String> listVehicles() {
        return ResponseEntity.ok("Vehicle controller ready");
    }
}
