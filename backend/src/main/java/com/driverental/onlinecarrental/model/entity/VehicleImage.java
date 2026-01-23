package com.driverental.onlinecarrental.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_images", indexes = {
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_vehicle_category", columnList = "vehicle_id,category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Image filename relative to the storage directory
    private String imageName;

    // Full path where the image is stored
    private String imagePath;

    // Category name - supports dynamic categories
    @Column(nullable = false)
    private String category;

    // Display order within the category
    @Builder.Default
    private Integer displayOrder = 0;

    // Is this image active/visible
    @Builder.Default
    private Boolean isActive = true;

    // Alt text for accessibility
    private String altText;

    // Image description
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreationTimestamp
    private LocalDateTime updatedAt;
}
