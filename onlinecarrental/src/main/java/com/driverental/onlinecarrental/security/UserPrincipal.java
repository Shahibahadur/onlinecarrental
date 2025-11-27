package com.driverental.onlinecarrental.security;

public class UserPrincipal {

    private final String username;

    public UserPrincipal(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
