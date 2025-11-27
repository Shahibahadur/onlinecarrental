package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserResponse userResponse);
    void deleteUser(Long id);
    UserResponse getCurrentUserProfile();
}