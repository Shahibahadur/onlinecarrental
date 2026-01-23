package com.driverental.onlinecarrental.controller;

import com.driverental.onlinecarrental.model.dto.request.ImageCategoryRequest;
import com.driverental.onlinecarrental.model.dto.response.ImageCategoryResponse;
import com.driverental.onlinecarrental.service.ImageCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/image-categories")
@RequiredArgsConstructor
public class AdminImageCategoryController {

    private final ImageCategoryService imageCategoryService;

    @GetMapping
    public ResponseEntity<List<ImageCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(imageCategoryService.getAllCategories());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ImageCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(imageCategoryService.getActiveCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageCategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(imageCategoryService.getCategoryById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ImageCategoryResponse> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(imageCategoryService.getCategoryByName(name));
    }

    @PostMapping
    public ResponseEntity<ImageCategoryResponse> createCategory(@RequestBody ImageCategoryRequest request) {
        ImageCategoryResponse response = imageCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageCategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody ImageCategoryRequest request) {
        ImageCategoryResponse response = imageCategoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        imageCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeDefaultCategories() {
        imageCategoryService.initializeDefaultCategories();
        return ResponseEntity.ok("Default categories initialized successfully");
    }
}
