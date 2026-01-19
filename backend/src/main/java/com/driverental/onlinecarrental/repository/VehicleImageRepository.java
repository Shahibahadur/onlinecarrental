package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.VehicleImage;
import com.driverental.onlinecarrental.model.enums.ImageCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {
    
    /**
     * Find all images for a specific vehicle
     */
    List<VehicleImage> findByVehicleIdAndIsActiveTrue(Long vehicleId);

    /**
     * Find images for a vehicle by category
     */
    List<VehicleImage> findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(
        Long vehicleId,
        ImageCategory category
    );

    /**
     * Find main image for a vehicle
     */
    VehicleImage findByVehicleIdAndCategoryAndIsActiveTrue(Long vehicleId, ImageCategory category);

    /**
     * Find all images for a category
     */
    List<VehicleImage> findByCategoryAndIsActiveTrueOrderByDisplayOrder(ImageCategory category);

    /**
     * Delete all images for a vehicle in a specific category
     */
    void deleteByVehicleIdAndCategory(Long vehicleId, ImageCategory category);

    /**
     * Count images for a vehicle
     */
    long countByVehicleIdAndIsActiveTrue(Long vehicleId);

    /**
     * Find paginated images for a vehicle
     */
    Page<VehicleImage> findByVehicleIdAndIsActiveTrueOrderByDisplayOrder(Long vehicleId, Pageable pageable);

    /**
     * Find paginated images for a vehicle by category
     */
    Page<VehicleImage> findByVehicleIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(
        Long vehicleId,
        ImageCategory category,
        Pageable pageable
    );
}
