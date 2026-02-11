package com.driverental.onlinecarrental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.esewa")
public class EsewaProperties {
    
    /**
     * eSewa merchant code (provided by eSewa)
     */
    private String productCode;
    
    /**
     * eSewa secret key for signature generation (provided by eSewa)
     */
    private String secretKey;
    
    /**
     * eSewa payment page URL
     * Sandbox: https://rc-epay.esewa.com.np/api/epay/transact
     * Live: https://epay.esewa.com.np/api/epay/transact
     */
    private String formUrl;
    
    /**
     * eSewa payment verification/status API URL
     * Sandbox: https://rc-epay.esewa.com.np/api/epay/status
     * Live: https://epay.esewa.com.np/api/epay/status
     */
    private String statusUrl;
    
    /**
     * Success callback URL (redirected after successful payment)
     */
    private String successUrl;
    
    /**
     * Failure callback URL (redirected after failed/cancelled payment)
     */
    private String failureUrl;
    
    /**
     * Default form URL for sandbox environment
     */
    public String getDefaultFormUrl() {
        return "https://rc-epay.esewa.com.np/api/epay/transact";
    }
    
    /**
     * Default status URL for sandbox environment
     */
    public String getDefaultStatusUrl() {
        return "https://rc-epay.esewa.com.np/api/epay/status";
    }
}
