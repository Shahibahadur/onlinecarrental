package com.driverental.onlinecarrental.model.dto.khalti;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KhaltiResponse {
    private String pidx;
    private String payment_url;
    private Long amount;
    private Long expires_at;
}
