package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.dto.SearchCriteria;
import com.driverental.onlinecarrental.model.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {
    Page<VehicleResponse> searchVehicles(SearchCriteria criteria, Pageable pageable);

    void rebuildSearchIndex();

    List<VehicleResponse> intelligentSearch(String query, String location, Pageable pageable);
}