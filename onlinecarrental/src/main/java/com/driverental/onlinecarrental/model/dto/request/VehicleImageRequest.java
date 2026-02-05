package com.driverental.onlinecarrental.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImageRequest {
    private Long vehicleId;
    private String imageName;
    private String category;
    private Integer displayOrder;
    private String altText;
    private String description;
    private Boolean isActive;
}
