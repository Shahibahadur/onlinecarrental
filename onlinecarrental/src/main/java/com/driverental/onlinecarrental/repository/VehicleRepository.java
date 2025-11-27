package com.driverental.onlinecarrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.driverental.onlinecarrental.model.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
