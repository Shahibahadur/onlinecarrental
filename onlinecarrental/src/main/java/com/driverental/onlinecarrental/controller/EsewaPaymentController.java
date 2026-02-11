package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.EsewaInitiateRequest;
import com.driverental.onlinecarrental.service.EsewaPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/esewa")
@RequiredArgsConstructor
@Tag(name = "Esewa Payment")
public class EsewaPaymentController {

    private final EsewaPaymentService esewaPaymentService;

    /**
     * Initiate eSewa payment
     * Returns the eSewa form parameters for redirecting user to eSewa payment page
     */
    @PostMapping("/initiate")
    @Operation(summary = "Initiate eSewa payment (returns params for eSewa form POST)")
    public ResponseEntity<Map<String, String>> initiate(@Valid @RequestBody EsewaInitiateRequest request) {
        return ResponseEntity.ok(esewaPaymentService.initiate(request));
    }

    /**
     * Verify eSewa payment status
     * This endpoint should be called after eSewa redirects back with transaction details
     */
    @GetMapping("/verify")
    @Operation(summary = "Verify eSewa payment status")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam("transaction_uuid") String transactionUuid,
            @RequestParam("total_amount") String totalAmount) {
        return ResponseEntity.ok(esewaPaymentService.verify(transactionUuid, totalAmount));
    }

    /**
     * eSewa success callback URL
     * eSewa will redirect here after successful payment
     */
    @GetMapping("/success")
    @Operation(summary = "eSewa payment success callback")
    public ResponseEntity<Map<String, Object>> paymentSuccess(
            @RequestParam("transaction_uuid") String transactionUuid,
            @RequestParam("total_amount") String totalAmount,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(esewaPaymentService.verify(transactionUuid, totalAmount));
    }

    /**
     * eSewa failure callback URL
     * eSewa will redirect here if payment fails
     */
    @GetMapping("/failure")
    @Operation(summary = "eSewa payment failure callback")
    public ResponseEntity<Map<String, String>> paymentFailure(
            @RequestParam("transaction_uuid") String transactionUuid,
            @RequestParam(value = "message", required = false) String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("transaction_uuid", transactionUuid);
        response.put("message", message != null ? message : "Payment was cancelled or failed");
        return ResponseEntity.ok(response);
    }
}
