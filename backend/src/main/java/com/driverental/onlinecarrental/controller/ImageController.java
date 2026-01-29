package com.driverental.onlinecarrental.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Image serving and upload APIs")
public class ImageController {

    @Value("${app.storage.vehicles-dir:../uploads/vehicles}")
    private String vehiclesDir;

    /**
     * Get vehicle image by vehicle type and filename
     */
    @GetMapping("/vehicles/{vehicleType}/{filename}")
    @Operation(summary = "Serve vehicle image by type and filename")
    public ResponseEntity<Resource> getVehicleImage(@PathVariable String vehicleType, @PathVariable String filename) throws Exception {
        String safeType = sanitizeVehicleType(vehicleType);
        Path file = Path.of(vehiclesDir).resolve(safeType).resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Get vehicle image by filename (legacy support)
     */
    @GetMapping("/vehicles/{filename}")
    @Operation(summary = "Serve vehicle image by filename (legacy)")
    public ResponseEntity<Resource> getVehicleImageLegacy(@PathVariable String filename) throws Exception {
        Path file = Path.of(vehiclesDir).resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Upload vehicle image with car type organization
     */
    @PostMapping("/vehicles/upload")
    @Operation(summary = "Upload vehicle image organized by vehicle type")
    public ResponseEntity<String> uploadVehicleImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "vehicleType", required = true) String vehicleType) throws Exception {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String safeType = sanitizeVehicleType(vehicleType);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        
        // Extract file extension
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0 && dot < originalFilename.length() - 1) {
            ext = originalFilename.substring(dot);
        } else {
            ext = ".jpg";
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // Create type-specific directory
        Path dir = Path.of(vehiclesDir).resolve(safeType);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("/api/images/vehicles/" + safeType + "/" + filename);
    }

    private String sanitizeVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.isBlank()) return "general";
        return vehicleType.trim().toLowerCase()
                .replaceAll("[^a-z0-9_-]", "");
    }
}
