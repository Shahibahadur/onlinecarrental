package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.entity.Vehicle;
import com.driverental.onlinecarrental.repository.VehicleRepository;
import com.driverental.onlinecarrental.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/images/vehicles")
@RequiredArgsConstructor
@Tag(name = "Admin Images", description = "Admin-only image management APIs")
public class AdminVehicleImageController {

    private final VehicleRepository vehicleRepository;
    private final ImageStorageService imageStorageService;

    public record MigrationResult(int total, int migrated, int skipped, int failed, List<Long> failedVehicleIds) {
    }

    @PostMapping("/migrate")
    @Operation(summary = "Bulk migrate vehicle images from external URLs into local uploads (Admin only)")
    public ResponseEntity<MigrationResult> migrateVehicleImages(
            @RequestParam(value = "max", required = false) Integer max
    ) {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        int limit = max != null && max > 0 ? Math.min(max, vehicles.size()) : vehicles.size();

        int migrated = 0;
        int skipped = 0;
        int failed = 0;
        List<Long> failedIds = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Vehicle v = vehicles.get(i);

            boolean hasLocal = v.getImageName() != null && !v.getImageName().isBlank();
            boolean hasExternal = v.getImageUrl() != null
                    && (v.getImageUrl().startsWith("http://") || v.getImageUrl().startsWith("https://"));

            if (hasLocal || !hasExternal) {
                skipped++;
                continue;
            }

            try {
                String category = v.getType() != null ? v.getType().name().toLowerCase(Locale.ROOT) : "general";
                String filename = imageStorageService.downloadVehicleImage(v.getImageUrl(), category);
                v.setImageName(filename);
                v.setImageCategory(category);
                v.setImageUrl(null);
                vehicleRepository.save(v);
                migrated++;
            } catch (Exception e) {
                failed++;
                failedIds.add(v.getId());
            }
        }

        return ResponseEntity.ok(new MigrationResult(limit, migrated, skipped, failed, failedIds));
    }
}
