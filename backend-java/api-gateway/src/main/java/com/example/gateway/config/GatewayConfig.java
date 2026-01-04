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
                // Auth Service Routes - using load balancer for service discovery
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE"))

                // User Management Routes (Auth Service)
                .route("user-management", r -> r
                        .path("/api/users/**")
                        .uri("lb://AUTH-SERVICE"))

                // Profile Service Routes
                .route("profile-service", r -> r
                        .path("/api/profiles/**")
                        .uri("lb://PROFILE-SERVICE"))

                // Loan Application Service Routes
                .route("loan-application-service", r -> r
                        .path("/api/loan-applications/**")
                        .uri("lb://LOAN-APPLICATION-SERVICE"))

                // Loan Service Routes
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri("lb://LOAN-SERVICE"))

                // Payment Service Routes (part of loan-service)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://LOAN-SERVICE"))

                // Report Service Routes
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .uri("lb://REPORT-SERVICE"))

                .build();
    }
}
