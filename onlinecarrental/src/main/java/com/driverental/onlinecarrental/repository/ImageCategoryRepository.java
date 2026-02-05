package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.ImageCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageCategoryRepository extends JpaRepository<ImageCategoryEntity, Long> {
    Optional<ImageCategoryEntity> findByName(String name);
    List<ImageCategoryEntity> findAllByIsActiveTrue();
    List<ImageCategoryEntity> findAllByIsDefaultTrue();
}
