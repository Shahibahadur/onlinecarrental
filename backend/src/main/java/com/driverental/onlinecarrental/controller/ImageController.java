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

    @Value("${app.storage.vehicles-dir:uploads/vehicles}")
    private String vehiclesDir;

    @GetMapping("/vehicles/{filename}")
    @Operation(summary = "Serve vehicle image by filename")
    public ResponseEntity<Resource> getVehicleImage(@PathVariable String filename) throws Exception {
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

    @PostMapping("/vehicles/upload")
    @Operation(summary = "Upload vehicle image (Admin only)")
    public ResponseEntity<String> uploadVehicleImage(@RequestParam("file") MultipartFile file) throws Exception {
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

        Path dir = Path.of(vehiclesDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("/api/images/vehicles/" + filename);
    }
}
