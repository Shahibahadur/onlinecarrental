package com.driverental.onlinecarrental.model.dto.request;

import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    private PaymentMethod paymentMethod;
}
