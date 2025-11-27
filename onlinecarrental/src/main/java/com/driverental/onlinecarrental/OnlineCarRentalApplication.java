package com.driverental.onlinecarrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class OnlineCarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineCarRentalApplication.class, args);
        System.out.println("🚗 Online Car Rental Platform Started Successfully!");
        System.out.println("📚 API Documentation: http://localhost:8080/api/swagger-ui.html");
        System.out.println("🔍 Health Check: http://localhost:8080/api/actuator/health");
    }
}