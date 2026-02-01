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

    @Value("${app.storage.vehicles-dir:uploads/vehicles}")
    private String vehiclesDir;

    private Path vehiclesBaseDir() {
        Path base = Path.of(vehiclesDir);
        if (!base.isAbsolute()) {
            // For relative paths, resolve them relative to the backend project root
            Path cwd = Path.of("").toAbsolutePath().normalize();
            base = cwd.resolve(vehiclesDir).normalize();
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
