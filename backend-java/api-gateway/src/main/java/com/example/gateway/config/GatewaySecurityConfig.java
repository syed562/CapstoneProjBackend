package com.example.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private final com.example.gateway.security.JwtReactiveAuthenticationManager authenticationManager;
    private final com.example.gateway.security.JwtServerAuthenticationConverter authenticationConverter;

    public GatewaySecurityConfig(
            com.example.gateway.security.JwtReactiveAuthenticationManager authenticationManager,
            com.example.gateway.security.JwtServerAuthenticationConverter authenticationConverter
    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationConverter = authenticationConverter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtWebFilter = new AuthenticationWebFilter(authenticationManager);
        jwtWebFilter.setServerAuthenticationConverter(authenticationConverter);

        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers(
                    "/api/auth/**",
                    "/api/users/**",
                    "/actuator/**",
                    "/eureka/**"
                ).permitAll()
                // Profile endpoints - require authentication
                .pathMatchers("/api/profiles/**").authenticated()
                // Payment endpoints - require authentication
                .pathMatchers("/api/payments/**").authenticated()
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
