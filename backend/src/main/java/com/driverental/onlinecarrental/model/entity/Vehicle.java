package com.driverental.onlinecarrental.model.entity;

import com.driverental.onlinecarrental.model.enums.FuelType;
import com.driverental.onlinecarrental.model.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String make;

    @NotBlank
    @Size(max = 50)
    private String model;

    @NotNull
    private Integer year;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @NotBlank
    @Size(max = 100)
    private String transmission;

    @NotNull
    private Integer seats;

    @NotNull
    private Integer luggageCapacity;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "vehicle_features", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal dailyPrice;

    @NotBlank
    @Size(max = 100)
    private String location;

    private String imageUrl;

    private String imageName;

    private String mainImageName;

    private String imageCategory;
    
    // Store multiple image names separated by comma for car type-based retrieval
    @Column(columnDefinition = "TEXT")
    private String imageNames;

    private String description;

    private String licensePlate;

    private Double engineCapacity;

    @NotNull
    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}
