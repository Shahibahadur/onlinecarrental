package com.driverental.onlinecarrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.driverental.onlinecarrental.model.entity.PriceConfig;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {
}
