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
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8083"))

                // User Management Routes (Auth Service)
                .route("user-management", r -> r
                        .path("/api/users/**")
                        .uri("http://localhost:8083"))

                // Profile Service Routes
                .route("profile-service", r -> r
                        .path("/api/profiles/**")
                        .uri("http://localhost:8082"))

                // Loan Application Service Routes
                .route("loan-application-service", r -> r
                        .path("/api/loan-applications/**")
                        .uri("http://localhost:8084"))

                // Loan Service Routes
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri("http://localhost:8085"))

                // Payment Service Routes (part of loan-service)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("http://localhost:8085"))

                // Report Service Routes
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .uri("http://localhost:8087"))

                .build();
    }
}
