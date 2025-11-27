package com.driverental.onlinecarrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.driverental.onlinecarrental.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
