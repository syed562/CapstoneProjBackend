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
                
                // Auth Service Swagger
                .route("auth-service-swagger", r -> r
                        .path("/auth-service/api-docs", "/auth-service/swagger-ui.html", "/auth-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/auth-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://auth-service"))
                
                // User routes (also handled by auth-service)
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://auth-service"))
                
                // Loan Service routes
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri("lb://loan-service"))
                
                // Loan Service Swagger
                .route("loan-service-swagger", r -> r
                        .path("/loan-service/api-docs", "/loan-service/swagger-ui.html", "/loan-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/loan-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://loan-service"))
                
                // Payment routes (also handled by loan-service)
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://loan-service"))
                
                // Loan Application Service routes
                .route("loan-application-service", r -> r
                        .path("/api/loan-applications/**")
                        .uri("lb://loan-application-service"))
                
                // Loan Application Service Swagger
                .route("loan-application-service-swagger", r -> r
                        .path("/loan-application-service/api-docs", "/loan-application-service/swagger-ui.html", "/loan-application-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/loan-application-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://loan-application-service"))
                
                // Profile Service routes
                .route("profile-service", r -> r
                        .path("/api/profiles/**")
                        .uri("lb://profile-service"))
                
                // Profile Service Swagger
                .route("profile-service-swagger", r -> r
                        .path("/profile-service/api-docs", "/profile-service/swagger-ui.html", "/profile-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/profile-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://profile-service"))
                
                // Notification Service routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                
                // Notification Service Swagger
                .route("notification-service-swagger", r -> r
                        .path("/notification-service/api-docs", "/notification-service/swagger-ui.html", "/notification-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/notification-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://notification-service"))
                
                // Report Service routes
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .uri("lb://report-service"))
                
                // Report Service Swagger
                .route("report-service-swagger", r -> r
                        .path("/report-service/api-docs", "/report-service/swagger-ui.html", "/report-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/report-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://report-service"))
                
                .build();
    }
}

