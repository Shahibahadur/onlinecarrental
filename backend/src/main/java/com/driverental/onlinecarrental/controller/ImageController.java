package com.driverental.onlinecarrental.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Image serving APIs")
public class ImageController {

    @Value("${app.storage.vehicles-dir:backend/uploads/vehicles}")
    private String vehiclesDir;

    private Path vehiclesBaseDir() {
        String normalized = vehiclesDir == null ? "" : vehiclesDir.replace('\\', '/');
        Path base = Path.of(vehiclesDir);
        if (!base.isAbsolute() && normalized.startsWith("backend/")) {
            Path cwd = Path.of("").toAbsolutePath().normalize();
            Path leaf = cwd.getFileName();
            if (leaf != null && leaf.toString().equalsIgnoreCase("backend")) {
                base = Path.of(normalized.substring("backend/".length()));
            }
        }
        return base;
    }

    private String sanitizeCategory(String category) {
        if (category == null || category.isBlank()) return "general";
        String c = category.trim().toLowerCase();
        c = c.replaceAll("[^a-z0-9_-]", "-");
        if (c.isBlank()) return "general";
        return c;
    }

    @GetMapping("/vehicles/{filename}")
    @Operation(summary = "Serve vehicle image by filename")
    public ResponseEntity<Resource> getVehicleImage(@PathVariable String filename) throws Exception {
        Path file = vehiclesBaseDir().resolve(filename).normalize();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            Path fallback = vehiclesBaseDir().resolve("general").resolve(filename).normalize();
            resource = new UrlResource(fallback.toUri());
            file = fallback;
        }

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    @GetMapping("/vehicles/{category}/{filename}")
    @Operation(summary = "Serve vehicle image by category and filename")
    public ResponseEntity<Resource> getVehicleImageByCategory(@PathVariable String category, @PathVariable String filename) throws Exception {
        String safeCategory = sanitizeCategory(category);
        Path file = vehiclesBaseDir().resolve(safeCategory).resolve(filename).normalize();
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

    @PostMapping("/vehicles/upload")
    @Operation(summary = "Upload vehicle image (Admin only)")
    public ResponseEntity<String> uploadVehicleImage(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "category", required = false) String category) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        String safeCategory = sanitizeCategory(category);
        Path dir = vehiclesBaseDir().resolve(safeCategory);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("/api/images/vehicles/" + safeCategory + "/" + filename);
    }
}
