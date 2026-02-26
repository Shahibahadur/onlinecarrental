package com.driverental.onlinecarrental.payment.khalti;

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
