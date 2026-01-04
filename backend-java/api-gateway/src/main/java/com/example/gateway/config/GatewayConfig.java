package com.example.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))
                
                // User routes (also handled by auth-service)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://auth-service"))
                
                // Loan Service routes
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri("lb://loan-service"))
                
                // Payment routes (also handled by loan-service)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://loan-service"))
                
                // Loan Application Service routes
                .route("loan-application-service", r -> r
                        .path("/api/loan-applications/**")
                        .uri("lb://loan-application-service"))
                
                // Profile Service routes
                .route("profile-service", r -> r
                        .path("/api/profiles/**")
                        .uri("lb://profile-service"))
                
                // Notification Service routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                
                // Report Service routes
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .uri("lb://report-service"))
                
                .build();
    }
}

