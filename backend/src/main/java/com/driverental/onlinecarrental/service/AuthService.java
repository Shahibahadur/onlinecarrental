package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.request.LoginRequest;
import com.driverental.onlinecarrental.model.dto.request.RegisterRequest;
import com.driverental.onlinecarrental.model.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}