package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.EsewaInitiateRequest;
import com.driverental.onlinecarrental.service.EsewaPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/esewa")
@RequiredArgsConstructor
@Tag(name = "Esewa Payment", description = "eSewa payment APIs")
public class EsewaPaymentController {

    private final EsewaPaymentService esewaPaymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate eSewa payment (returns params for eSewa form POST)")
    public ResponseEntity<Map<String, String>> initiate(@Valid @RequestBody EsewaInitiateRequest request) {
        return ResponseEntity.ok(esewaPaymentService.initiate(request));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify eSewa payment status")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam("uuid") String uuid,
            @RequestParam("amount") String amount) {
        return ResponseEntity.ok(esewaPaymentService.verify(uuid, amount));
    }
}
