package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.khalti.KhaltiCallbackDTO;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiRequest;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiResponse;
import com.driverental.onlinecarrental.service.KhaltiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Khalti Payment", description = "Khalti payment gateway APIs")
public class KhaltiController {

    private final KhaltiService khaltiService;

    @PostMapping("/khalti-initiate")
    @Operation(summary = "Initiate Khalti payment")
    public ResponseEntity<KhaltiResponse> initiatePayment(@RequestBody KhaltiRequest request) {
        KhaltiResponse response = khaltiService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/khalti-response-handle")
    @Operation(summary = "Handle Khalti payment callback response")
    public RedirectView handleResponse(KhaltiCallbackDTO values) {
        boolean isVerified = khaltiService.verifyPayment(values);
        
        String query = "amount=" + URLEncoder.encode(String.valueOf((Long.parseLong(values.getAmount()) * 0.01)), StandardCharsets.UTF_8)
                + "&transactionId=" + URLEncoder.encode(values.getTransaction_id(), StandardCharsets.UTF_8);
        
        if (isVerified) {
            return new RedirectView("http://localhost:5173/payment/success?" + query);
        } else {
            return new RedirectView("http://localhost:5173/payment/failure?" + query);
        }
    }
}
