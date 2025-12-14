package com.driverental.onlinecarrental.model.entity;

import com.driverental.onlinecarrental.model.enums.FuelType;
import com.driverental.onlinecarrental.model.enums.CarCategory;
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
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {
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
    private CarCategory type;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @NotBlank
    @Size(max = 100)
    private String transmission;

    @NotNull
    private Integer seats;

    @NotNull
    private Integer luggageCapacity;

    @ElementCollection
    @CollectionTable(name = "car_features", joinColumns = @JoinColumn(name = "car_id"))
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

    @NotNull
    private Boolean isAvailable = true;

    private Double rating = 0.0;

    private Integer reviewCount = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}