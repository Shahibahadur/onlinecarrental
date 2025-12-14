package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.algorithm.aho_corasick.AhoCorasick;
import com.driverental.onlinecarrental.algorithm.aho_corasick.SearchResult;
import com.driverental.onlinecarrental.model.dto.SearchCriteria;
import com.driverental.onlinecarrental.model.dto.response.CarResponse;
import com.driverental.onlinecarrental.model.entity.Car;
import com.driverental.onlinecarrental.repository.CarRepository;
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

    private final CarRepository carRepository;
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
        List<Car> allCars = carRepository.findAll();
        searchKeywords = extractKeywords(allCars);
        ahoCorasick.buildTrie(searchKeywords);
        log.info("Search index rebuilt with {} keywords", searchKeywords.size());
    }

    private List<String> extractKeywords(List<Car> cars) {
        Set<String> keywords = new HashSet<>();
        for (Car car : cars) {
            keywords.add(car.getMake().toLowerCase());
            keywords.add(car.getModel().toLowerCase());
            keywords.add(car.getType().name().toLowerCase());
            keywords.add(car.getFuelType().name().toLowerCase());
            keywords.add(car.getTransmission().toLowerCase());
            keywords.add(car.getLocation().toLowerCase());
            keywords.addAll(car.getFeatures().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet()));
        }
        return new ArrayList<>(keywords);
    }

    @Override
    @Cacheable(value = "searchResults", key = "#criteria.hashCode() + '-' + #pageable.pageNumber")
    public Page<CarResponse> searchCars(SearchCriteria criteria, Pageable pageable) {
        Specification<Car> spec = buildSpecification(criteria);
        Page<Car> cars = carRepository.findAll(spec, pageable);
        return cars.map(this::convertToResponse);
    }

    @Override
    @Cacheable(value = "intelligentSearch", key = "#query + '-' + #location + '-' + #pageable.pageNumber")
    public List<CarResponse> intelligentSearch(String query, String location, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            // Fall back to regular search if no query provided
            SearchCriteria criteria = SearchCriteria.builder()
                    .location(location)
                    .build();
            return searchCars(criteria, pageable).getContent();
        }

        List<SearchResult> matches = ahoCorasick.search(query);
        Set<String> matchedKeywords = matches.stream()
                .map(SearchResult::getKeyword)
                .collect(Collectors.toSet());

        List<Car> cars = carRepository.findByIntelligentSearch(matchedKeywords, location, pageable);
        return cars.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private Specification<Car> buildSpecification(SearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by availability
            predicates.add(cb.equal(root.get("isAvailable"), true));

            if (criteria.getLocation() != null && !criteria.getLocation().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + criteria.getLocation().toLowerCase() + "%"));
            }

            if (criteria.getType() != null) {
                predicates.add(cb.equal(root.get("type"), criteria.getType()));
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

    private CarResponse convertToResponse(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .year(car.getYear())
                .type(car.getType())
                .fuelType(car.getFuelType())
                .transmission(car.getTransmission())
                .seats(car.getSeats())
                .luggageCapacity(car.getLuggageCapacity())
                .features(car.getFeatures())
                .basePrice(car.getBasePrice())
                .dailyPrice(car.getDailyPrice())
                .location(car.getLocation())
                .imageUrl(car.getImageUrl())
                .isAvailable(car.getIsAvailable())
                .rating(car.getRating())
                .reviewCount(car.getReviewCount())
                .createdAt(car.getCreatedAt())
                .build();
    }
}