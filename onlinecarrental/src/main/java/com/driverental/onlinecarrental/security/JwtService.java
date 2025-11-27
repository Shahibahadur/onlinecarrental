package com.driverental.onlinecarrental.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String generateToken(String subject) {
        return subject + "-token";
    }
}
