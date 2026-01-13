package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    List<Vehicle> findByIsAvailableTrue();

    Page<Vehicle> findByIsAvailableTrue(Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.isAvailable = true AND (" +
            ":search IS NULL OR TRIM(:search) = '' OR " +
            "LOWER(v.make) LIKE CONCAT('%', LOWER(:search), '%') OR " +
            "LOWER(v.model) LIKE CONCAT('%', LOWER(:search), '%') OR " +
            "LOWER(v.location) LIKE CONCAT('%', LOWER(:search), '%') OR " +
            "LOWER(v.type) LIKE CONCAT('%', LOWER(:search), '%')" +
            ")")
    Page<Vehicle> findAvailableBySearch(@Param("search") String search, Pageable pageable);

    Page<Vehicle> findByTypeAndIsAvailableTrue(VehicleType type, Pageable pageable);

    Page<Vehicle> findByLocationContainingIgnoreCaseAndIsAvailableTrue(String location, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.dailyPrice BETWEEN :minPrice AND :maxPrice AND v.isAvailable = true")
    Page<Vehicle> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE :feature MEMBER OF v.features AND v.isAvailable = true")
    Page<Vehicle> findByFeature(@Param("feature") String feature, Pageable pageable);

    List<Vehicle> findTop10ByOrderByRatingDescReviewCountDesc();

    List<Vehicle> findTop10ByIsAvailableTrueOrderByRatingDescReviewCountDesc();

    @Query("SELECT v FROM Vehicle v WHERE " +
            "(LOWER(v.make) IN :keywords OR " +
            "LOWER(v.model) IN :keywords OR " +
            "LOWER(v.type) IN :keywords OR " +
            "EXISTS (SELECT f FROM v.features f WHERE LOWER(f) IN :keywords)) AND " +
            "(:location IS NULL OR LOWER(v.location) LIKE CONCAT('%', LOWER(:location), '%')) AND " +
            "v.isAvailable = true")
    List<Vehicle> findByIntelligentSearch(@Param("keywords") Set<String> keywords,
            @Param("location") String location,
            Pageable pageable);
}
