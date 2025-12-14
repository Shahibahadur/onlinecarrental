package com.driverental.onlinecarrental.repository;

import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.model.enums.CarCategory;
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
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

       List<Car> findByIsAvailableTrue();

       Page<Car> findByIsAvailableTrue(Pageable pageable);

       Page<Car> findByTypeAndIsAvailableTrue(CarCategory type, Pageable pageable);

       Page<Car> findByLocationContainingIgnoreCaseAndIsAvailableTrue(String location, Pageable pageable);

       @Query("SELECT c FROM Car c WHERE c.dailyPrice BETWEEN :minPrice AND :maxPrice AND c.isAvailable = true")
       Page<Car> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice,
                     Pageable pageable);

       @Query("SELECT c FROM Car c WHERE :feature MEMBER OF c.features AND c.isAvailable = true")
       Page<Car> findByFeature(@Param("feature") String feature, Pageable pageable);

       List<Car> findTop10ByOrderByRatingDescReviewCountDesc();

       @Query("SELECT c FROM Car c WHERE " +
                     "(LOWER(c.make) IN :keywords OR " +
                     "LOWER(c.model) IN :keywords OR " +
                     "LOWER(c.type) IN :keywords OR " +
                     "EXISTS (SELECT f FROM c.features f WHERE LOWER(f) IN :keywords)) AND " +
                     "(c.location LIKE %:location% OR :location IS NULL) AND " +
                     "c.isAvailable = true")
       List<Car> findByIntelligentSearch(@Param("keywords") Set<String> keywords,
                     @Param("location") String location,
                     Pageable pageable);
}