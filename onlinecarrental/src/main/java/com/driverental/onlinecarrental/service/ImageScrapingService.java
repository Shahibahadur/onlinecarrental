package com.driverental.onlinecarrental.service;

import java.io.IOException;

public interface ImageScrapingService {
    /**
     * Scrape and download car images from web
     * @param carBrand Brand of the car (e.g., Toyota, Honda)
     * @param carModel Model of the car (e.g., Corolla, Civic)
     * @param vehicleType Type of vehicle (sedan, suv, etc)
     * @return List of downloaded image filenames
     */
    java.util.List<String> scrapeAndDownloadCarImages(String carBrand, String carModel, String vehicleType) throws IOException;

    /**
     * Download image from URL to local storage
     * @param imageUrl URL of the image to download
     * @param vehicleType Type of vehicle for folder organization
     * @return Filename of the downloaded image
     */
    String downloadImageFromUrl(String imageUrl, String vehicleType) throws IOException;

    /**
     * Search images from Unsplash API
     */
    java.util.List<String> searchUnsplashImages(String query, String vehicleType, int count) throws IOException;

    /**
     * Search images from Pexels API
     */
    java.util.List<String> searchPexelsImages(String query, String vehicleType, int count) throws IOException;
}
