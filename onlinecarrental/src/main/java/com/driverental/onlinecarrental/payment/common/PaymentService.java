package com.driverental.onlinecarrental.payment.common;

import com.driverental.onlinecarrental.model.dto.request.PaymentRequest;
import com.driverental.onlinecarrental.model.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPaymentByBooking(Long bookingId);
}
