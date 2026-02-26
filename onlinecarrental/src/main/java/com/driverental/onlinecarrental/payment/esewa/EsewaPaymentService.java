package com.driverental.onlinecarrental.payment.esewa;

import java.util.Map;

public interface EsewaPaymentService {
    Map<String, String> initiate(EsewaInitiateRequest request);

    Map<String, Object> verify(String uuid, String amount);
}
