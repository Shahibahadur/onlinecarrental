package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    @Value("${app.storage.vehicles-dir:uploads/vehicles}")
    private String vehiclesDir;

    @Override
    public String downloadVehicleImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IOException("imageUrl is blank");
        }

        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setInstanceFollowRedirects(true);

        String contentType = connection.getContentType();
        String ext = extensionFromContentTypeOrUrl(contentType, url.getPath());
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Path.of(vehiclesDir);
        Files.createDirectories(dir);

        Path target = dir.resolve(filename);

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }

        return filename;
    }

    private String extensionFromContentTypeOrUrl(String contentType, String urlPath) {
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT);
            if (ct.contains("image/jpeg") || ct.contains("image/jpg")) return ".jpg";
            if (ct.contains("image/png")) return ".png";
            if (ct.contains("image/webp")) return ".webp";
            if (ct.contains("image/gif")) return ".gif";
        }

        if (urlPath != null) {
            String p = urlPath.toLowerCase(Locale.ROOT);
            if (p.endsWith(".jpg") || p.endsWith(".jpeg")) return ".jpg";
            if (p.endsWith(".png")) return ".png";
            if (p.endsWith(".webp")) return ".webp";
            if (p.endsWith(".gif")) return ".gif";
        }

        return ".jpg";
    }
}
