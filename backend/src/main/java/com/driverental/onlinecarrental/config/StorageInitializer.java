package com.driverental.onlinecarrental.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class StorageInitializer implements ApplicationRunner {

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path base = vehiclesBaseDir();
        Files.createDirectories(base);
        Files.createDirectories(base.resolve("general"));
    }
}
