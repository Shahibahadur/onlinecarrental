package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.khalti.KhaltiCallbackDTO;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiRequest;
import com.driverental.onlinecarrental.model.dto.khalti.KhaltiResponse;

public interface KhaltiService {
    KhaltiResponse initiatePayment(KhaltiRequest khaltiRequest);
    
    boolean verifyPayment(KhaltiCallbackDTO response);
    
    String generateUniqueId();
}
