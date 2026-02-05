package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.service.ImageScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageScrapingServiceImpl implements ImageScrapingService {

    @Value("${app.storage.vehicles-dir:backend/uploads/vehicles}")
    private String vehiclesDir;

    @Value("${app.scraping.unsplash-api-key:#{null}}")
    private String unsplashApiKey;

    @Value("${app.scraping.pexels-api-key:#{null}}")
    private String pexelsApiKey;

    @Override
    public List<String> scrapeAndDownloadCarImages(String carBrand, String carModel, String vehicleType) throws IOException {
        List<String> downloadedImages = new ArrayList<>();
        
        log.info("Scraping images for {} {}", carBrand, carModel);

        // Try multiple sources
        String searchQuery = carBrand + " " + carModel;

        // Try Unsplash first
        if (unsplashApiKey != null && !unsplashApiKey.isBlank()) {
            try {
                downloadedImages.addAll(searchUnsplashImages(searchQuery, vehicleType, 5));
                log.info("Downloaded {} images from Unsplash", downloadedImages.size());
            } catch (Exception e) {
                log.warn("Failed to scrape from Unsplash: {}", e.getMessage());
            }
        }

        // Try Pexels
        if (downloadedImages.size() < 3 && pexelsApiKey != null && !pexelsApiKey.isBlank()) {
            try {
                downloadedImages.addAll(searchPexelsImages(searchQuery, vehicleType, 5));
                log.info("Downloaded {} images from Pexels", downloadedImages.size());
            } catch (Exception e) {
                log.warn("Failed to scrape from Pexels: {}", e.getMessage());
            }
        }

        return downloadedImages;
    }

    @Override
    public String downloadImageFromUrl(String imageUrl, String vehicleType) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IOException("Image URL is empty");
        }

        log.info("Downloading image from: {}", imageUrl);

        // Create vehicle type folder if it doesn't exist
        Path typeDir = Path.of(vehiclesDir, vehicleType.toLowerCase());
        Files.createDirectories(typeDir);

        // Extract filename or generate one
        String filename = extractFilenameFromUrl(imageUrl);
        if (filename == null || filename.isEmpty()) {
            filename = UUID.randomUUID().toString() + ".jpg";
        }

        Path targetPath = typeDir.resolve(filename);

        // Download the file
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setInstanceFollowRedirects(true);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download image. Response code: " + responseCode);
        }

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }

        log.info("Image downloaded successfully: {}", filename);
        return filename;
    }

    @Override
    public List<String> searchUnsplashImages(String query, String vehicleType, int count) throws IOException {
        List<String> downloadedImages = new ArrayList<>();

        if (unsplashApiKey == null || unsplashApiKey.isBlank()) {
            log.warn("Unsplash API key not configured");
            return downloadedImages;
        }

        try {
            String url = "https://api.unsplash.com/search/photos?query=" + query.replace(" ", "+")
                    + "&count=" + count + "&client_id=" + unsplashApiKey;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                
                // Parse JSON response (simple parsing, use Jackson for production)
                String[] urls = response.split("\"raw\":\"");
                for (int i = 1; i < urls.length && downloadedImages.size() < count; i++) {
                    String imageUrl = urls[i].split("\"")[0];
                    try {
                        String filename = downloadImageFromUrl(imageUrl, vehicleType);
                        downloadedImages.add(filename);
                    } catch (Exception e) {
                        log.warn("Failed to download image: {}", e.getMessage());
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error searching Unsplash: {}", e.getMessage());
        }

        return downloadedImages;
    }

    @Override
    public List<String> searchPexelsImages(String query, String vehicleType, int count) throws IOException {
        List<String> downloadedImages = new ArrayList<>();

        if (pexelsApiKey == null || pexelsApiKey.isBlank()) {
            log.warn("Pexels API key not configured");
            return downloadedImages;
        }

        try {
            String url = "https://api.pexels.com/v1/search?query=" + query.replace(" ", "+")
                    + "&per_page=" + count;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("Authorization", pexelsApiKey);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                
                // Parse JSON response (simple parsing, use Jackson for production)
                String[] urls = response.split("\"src\":");
                for (int i = 1; i < urls.length && downloadedImages.size() < count; i++) {
                    String section = urls[i].split("},")[0];
                    String imageUrl = section.split("\"original\":\"")[1].split("\"")[0];
                    try {
                        String filename = downloadImageFromUrl(imageUrl, vehicleType);
                        downloadedImages.add(filename);
                    } catch (Exception e) {
                        log.warn("Failed to download image: {}", e.getMessage());
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error searching Pexels: {}", e.getMessage());
        }

        return downloadedImages;
    }

    private String extractFilenameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}
