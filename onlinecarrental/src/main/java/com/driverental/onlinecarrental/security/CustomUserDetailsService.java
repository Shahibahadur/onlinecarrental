package com.driverental.onlinecarrental.security;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService {

    public UserPrincipal loadUserByUsername(String username) {
        return new UserPrincipal(username);
    }
}
