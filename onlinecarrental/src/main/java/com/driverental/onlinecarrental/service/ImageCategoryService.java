package com.driverental.onlinecarrental.service;

import com.driverental.onlinecarrental.model.entity.ImageCategoryEntity;
import com.driverental.onlinecarrental.model.dto.request.ImageCategoryRequest;
import com.driverental.onlinecarrental.model.dto.response.ImageCategoryResponse;

import java.util.List;

public interface ImageCategoryService {
    List<ImageCategoryResponse> getAllCategories();
    List<ImageCategoryResponse> getActiveCategories();
    ImageCategoryResponse getCategoryById(Long id);
    ImageCategoryResponse getCategoryByName(String name);
    ImageCategoryResponse createCategory(ImageCategoryRequest request);
    ImageCategoryResponse updateCategory(Long id, ImageCategoryRequest request);
    void deleteCategory(Long id);
    void initializeDefaultCategories();
}
