package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.PaymentRequest;
import com.driverental.onlinecarrental.model.dto.response.PaymentResponse;
import com.driverental.onlinecarrental.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create or update a payment record for a booking")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment info for a booking")
    public ResponseEntity<PaymentResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }
}
