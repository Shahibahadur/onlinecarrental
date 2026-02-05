package com.driverental.onlinecarrental.service.impl;

import com.driverental.onlinecarrental.model.entity.ImageCategoryEntity;
import com.driverental.onlinecarrental.model.dto.request.ImageCategoryRequest;
import com.driverental.onlinecarrental.model.dto.response.ImageCategoryResponse;
import com.driverental.onlinecarrental.repository.ImageCategoryRepository;
import com.driverental.onlinecarrental.service.ImageCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageCategoryServiceImpl implements ImageCategoryService {

    private final ImageCategoryRepository imageCategoryRepository;

    private static final String[] DEFAULT_CATEGORIES = {
        "SEDAN", "SPORTS", "CONVERTIBLE", "HATCHBACK", "HYBRID", "ELECTRIC", "SUV"
    };

    private static final String[] DEFAULT_DESCRIPTIONS = {
        "Sedan vehicles",
        "Sports cars",
        "Convertible vehicles",
        "Hatchback vehicles",
        "Hybrid vehicles",
        "Electric vehicles",
        "SUV/Crossover vehicles"
    };

    @Override
    public List<ImageCategoryResponse> getAllCategories() {
        return imageCategoryRepository.findAll()
            .stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<ImageCategoryResponse> getActiveCategories() {
        return imageCategoryRepository.findAllByIsActiveTrue()
            .stream()
            .map(this::entityToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public ImageCategoryResponse getCategoryById(Long id) {
        return imageCategoryRepository.findById(id)
            .map(this::entityToResponse)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public ImageCategoryResponse getCategoryByName(String name) {
        return imageCategoryRepository.findByName(name)
            .map(this::entityToResponse)
            .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
    }

    @Override
    @Transactional
    public ImageCategoryResponse createCategory(ImageCategoryRequest request) {
        if (imageCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        ImageCategoryEntity entity = ImageCategoryEntity.builder()
            .name(request.getName())
            .description(request.getDescription())
            .isDefault(false)
            .isActive(true)
            .build();

        ImageCategoryEntity saved = imageCategoryRepository.save(entity);
        return entityToResponse(saved);
    }

    @Override
    @Transactional
    public ImageCategoryResponse updateCategory(Long id, ImageCategoryRequest request) {
        ImageCategoryEntity entity = imageCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (!entity.getName().equals(request.getName()) && 
            imageCategoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        ImageCategoryEntity updated = imageCategoryRepository.save(entity);
        return entityToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ImageCategoryEntity entity = imageCategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (entity.getIsDefault()) {
            throw new RuntimeException("Cannot delete default category: " + entity.getName());
        }

        imageCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void initializeDefaultCategories() {
        // Check if categories already exist
        if (imageCategoryRepository.count() > 0) {
            return;
        }

        // Create default categories
        for (int i = 0; i < DEFAULT_CATEGORIES.length; i++) {
            ImageCategoryEntity entity = ImageCategoryEntity.builder()
                .name(DEFAULT_CATEGORIES[i])
                .description(DEFAULT_DESCRIPTIONS[i])
                .isDefault(true)
                .isActive(true)
                .build();
            imageCategoryRepository.save(entity);
        }
    }

    private ImageCategoryResponse entityToResponse(ImageCategoryEntity entity) {
        return ImageCategoryResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .isDefault(entity.getIsDefault())
            .isActive(entity.getIsActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
