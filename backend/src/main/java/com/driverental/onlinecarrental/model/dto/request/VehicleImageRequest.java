package com.driverental.onlinecarrental.model.dto.request;

import com.driverental.onlinecarrental.model.enums.ImageCategory;
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
    private ImageCategory category;
    private Integer displayOrder;
    private String altText;
    private String description;
    private Boolean isActive;
}
