package com.driverental.onlinecarrental.model.dto.khalti;

import com.driverental.onlinecarrental.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KhaltiRequest {
    private Long amount;
    private String purchase_order_id;
    private String purchase_order_name;
}
