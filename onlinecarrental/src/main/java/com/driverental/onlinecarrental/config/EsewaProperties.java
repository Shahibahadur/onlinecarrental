package com.driverental.onlinecarrental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.esewa")
public class EsewaProperties {
    private String productCode;
    private String secretKey;
    private String formUrl;
    private String statusUrl;
    private String successUrl;
    private String failureUrl;
}
