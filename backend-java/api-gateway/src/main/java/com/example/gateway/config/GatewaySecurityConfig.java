package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

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

    jwtWebFilter.setAuthenticationFailureHandler((exchange, ex) -> {
        exchange.getExchange().getResponse()
            .setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getExchange().getResponse().setComplete();
    });

    jwtWebFilter.setRequiresAuthenticationMatcher(
        ServerWebExchangeMatchers.pathMatchers(
            "/api/profiles/**",
            "/api/loan-applications/**",
            "/api/payments/**",
            "/api/loans/**",
            "/api/reports/**",
            "/api/rates/**"
        )
    );

    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .pathMatchers("/api/auth/**", "/api/users/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/profiles/**").permitAll()
            // Allow Swagger UI and API docs endpoints
            .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
            .pathMatchers("/*/api-docs", "/*/swagger-ui.html", "/*/swagger-ui/**").permitAll()
            .anyExchange().authenticated()
        )
        .addFilterAt(jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*") );
    config.setAllowCredentials(false);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
    source.registerCorsConfiguration("/**", config);
    return source;
}
}
