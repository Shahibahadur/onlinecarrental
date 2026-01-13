package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.EsewaInitiateRequest;

import java.util.Map;

public interface EsewaPaymentService {
    Map<String, String> initiate(EsewaInitiateRequest request);

    Map<String, Object> verify(String uuid, String amount);
}
