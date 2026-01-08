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
               
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://auth-service"))
                
                
                .route("auth-service-swagger", r -> r
                        .path("/auth-service/api-docs", "/auth-service/swagger-ui.html", "/auth-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/auth-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://auth-service"))
                
             
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .uri("lb://auth-service"))
                
                
                .route("loan-service", r -> r
                        .path("/api/loans/**")
                        .uri("lb://loan-service"))
                
              
                .route("loan-service-swagger", r -> r
                        .path("/loan-service/api-docs", "/loan-service/swagger-ui.html", "/loan-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/loan-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://loan-service"))
                
               
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://loan-service"))
                
               
                .route("loan-application-service", r -> r
                        .path("/api/loan-applications/**")
                        .uri("lb://loan-application-service"))
                
               
                .route("loan-application-service-swagger", r -> r
                        .path("/loan-application-service/api-docs", "/loan-application-service/swagger-ui.html", "/loan-application-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/loan-application-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://loan-application-service"))
                
               
                .route("profile-service", r -> r
                        .path("/api/profiles/**")
                        .uri("lb://profile-service"))
                
                
                .route("profile-service-swagger", r -> r
                        .path("/profile-service/api-docs", "/profile-service/swagger-ui.html", "/profile-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/profile-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://profile-service"))
                
              
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                
              
                .route("notification-service-swagger", r -> r
                        .path("/notification-service/api-docs", "/notification-service/swagger-ui.html", "/notification-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/notification-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://notification-service"))
                
              
                .route("report-service", r -> r
                        .path("/api/reports/**")
                        .uri("lb://report-service"))
                
                
                .route("report-service-swagger", r -> r
                        .path("/report-service/api-docs", "/report-service/swagger-ui.html", "/report-service/swagger-ui/**")
                        .filters(f -> f.rewritePath("/report-service/(?<segment>.*)", "/${segment}"))
                        .uri("lb://report-service"))
                
                .build();
    }
}

