package com.driverental.onlinecarrental.payment.khalti;

public interface KhaltiService {
    KhaltiResponse initiatePayment(KhaltiRequest khaltiRequest);
    
    boolean verifyPayment(KhaltiCallbackDTO response);
    
    String generateUniqueId();
}
