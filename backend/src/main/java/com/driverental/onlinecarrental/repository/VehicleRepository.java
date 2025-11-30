package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Page<Vehicle> findByIsAvailableTrue(Pageable pageable);
    Page<Vehicle> findByTypeAndIsAvailableTrue(VehicleType type, Pageable pageable);
    Page<Vehicle> findByLocationContainingIgnoreCaseAndIsAvailableTrue(String location, Pageable pageable);
    
    @Query("SELECT v FROM Vehicle v WHERE v.dailyPrice BETWEEN :minPrice AND :maxPrice AND v.isAvailable = true")
    Page<Vehicle> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);
    
    @Query("SELECT v FROM Vehicle v WHERE :feature MEMBER OF v.features AND v.isAvailable = true")
    Page<Vehicle> findByFeature(@Param("feature") String feature, Pageable pageable);
    
    List<Vehicle> findTop10ByOrderByRatingDescReviewCountDesc();
    
    @Query("SELECT v FROM Vehicle v WHERE " +
           "(LOWER(v.make) IN :keywords OR " +
           "LOWER(v.model) IN :keywords OR " +
           "LOWER(v.type) IN :keywords OR " +
           "EXISTS (SELECT f FROM v.features f WHERE LOWER(f) IN :keywords)) AND " +
           "(v.location LIKE %:location% OR :location IS NULL) AND " +
           "v.isAvailable = true")
    List<Vehicle> findByIntelligentSearch(@Param("keywords") Set<String> keywords,
                                         @Param("location") String location,
                                         Pageable pageable);
}