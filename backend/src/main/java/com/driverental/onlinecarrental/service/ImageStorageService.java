package com.driverental.onlinecarrental.service;

import java.io.IOException;

public interface ImageStorageService {
    String downloadVehicleImage(String imageUrl, String category) throws IOException;
}
