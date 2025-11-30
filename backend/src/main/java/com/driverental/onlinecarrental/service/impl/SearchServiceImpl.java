package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.aho_corasick.AhoCorasick;
import com.driverental.onlinecarrental.algorithm.aho_corasick.SearchResult;
import com.driverental.onlinecarrental.model.dto.SearchCriteria;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.SearchService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    
    private final VehicleRepository vehicleRepository;
    private final AhoCorasick ahoCorasick;
    private List<String> searchKeywords;
    
    @PostConstruct
    public void init() {
        rebuildSearchIndex();
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Rebuild every hour
    public void rebuildSearchIndex() {
        log.info("Rebuilding search index...");
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        searchKeywords = extractKeywords(allVehicles);
        ahoCorasick.buildTrie(searchKeywords);
        log.info("Search index rebuilt with {} keywords", searchKeywords.size());
    }
    
    private List<String> extractKeywords(List<Vehicle> vehicles) {
        Set<String> keywords = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            keywords.add(vehicle.getMake().toLowerCase());
            keywords.add(vehicle.getModel().toLowerCase());
            keywords.add(vehicle.getType().name().toLowerCase());
            keywords.add(vehicle.getFuelType().name().toLowerCase());
            keywords.add(vehicle.getTransmission().toLowerCase());
            keywords.add(vehicle.getLocation().toLowerCase());
            keywords.addAll(vehicle.getFeatures().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet()));
        }
        return new ArrayList<>(keywords);
    }
    
    @Override
    @Cacheable(value = "searchResults", key = "#criteria.hashCode() + '-' + #pageable.pageNumber")
    public Page<VehicleResponse> searchVehicles(SearchCriteria criteria, Pageable pageable) {
        Specification<Vehicle> spec = buildSpecification(criteria);
        Page<Vehicle> vehicles = vehicleRepository.findAll(spec, pageable);
        return vehicles.map(this::convertToResponse);
    }
    
    @Override
    @Cacheable(value = "intelligentSearch", key = "#query + '-' + #location + '-' + #pageable.pageNumber")
    public List<VehicleResponse> intelligentSearch(String query, String location, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            // Fall back to regular search if no query provided
            SearchCriteria criteria = SearchCriteria.builder()
                    .location(location)
                    .build();
            return searchVehicles(criteria, pageable).getContent();
        }
        
        List<SearchResult> matches = ahoCorasick.search(query);
        Set<String> matchedKeywords = matches.stream()
                .map(SearchResult::getKeyword)
                .collect(Collectors.toSet());
        
        List<Vehicle> vehicles = vehicleRepository.findByIntelligentSearch(matchedKeywords, location, pageable);
        return vehicles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private Specification<Vehicle> buildSpecification(SearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter by availability
            predicates.add(cb.equal(root.get("isAvailable"), true));
            
            if (criteria.getLocation() != null && !criteria.getLocation().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), 
                    "%" + criteria.getLocation().toLowerCase() + "%"));
            }
            
            if (criteria.getVehicleType() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.getVehicleType()));
            }
            
            if (criteria.getFuelType() != null) {
                predicates.add(cb.equal(root.get("fuelType"), criteria.getFuelType()));
            }
            
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dailyPrice"), criteria.getMinPrice()));
            }
            
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dailyPrice"), criteria.getMaxPrice()));
            }
            
            if (criteria.getFeatures() != null && !criteria.getFeatures().isEmpty()) {
                for (String feature : criteria.getFeatures()) {
                    predicates.add(cb.isMember(feature, root.get("features")));
                }
            }
            
            if (criteria.getMinSeats() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("seats"), criteria.getMinSeats()));
            }
            
            if (criteria.getMaxSeats() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("seats"), criteria.getMaxSeats()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    private VehicleResponse convertToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .type(vehicle.getType())
                .fuelType(vehicle.getFuelType())
                .transmission(vehicle.getTransmission())
                .seats(vehicle.getSeats())
                .luggageCapacity(vehicle.getLuggageCapacity())
                .features(vehicle.getFeatures())
                .basePrice(vehicle.getBasePrice())
                .dailyPrice(vehicle.getDailyPrice())
                .location(vehicle.getLocation())
                .imageUrl(vehicle.getImageUrl())
                .isAvailable(vehicle.getIsAvailable())
                .rating(vehicle.getRating())
                .reviewCount(vehicle.getReviewCount())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}