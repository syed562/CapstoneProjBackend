package com.example.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Authentication Filter for API Gateway
 * Extracts JWT token from Authorization header and validates it
 * Passes user information (userId, role, username) to downstream services
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation12345678}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String token = extractToken(exchange.getRequest().getHeaders().getFirst("Authorization"));

            if (token != null && validateToken(token)) {
                Claims claims = extractClaims(token);
                String userId = claims.getSubject();
                String role = (String) claims.get("role");
                String username = (String) claims.get("username");

                // Add user information to request headers for downstream services
                exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .header("X-User-Name", username)
                        .build();
            }
        } catch (Exception e) {
            // Token validation failed - continue without authentication
            // Downstream services will handle 401 for protected endpoints
        }

        return chain.filter(exchange);
    }

    /**
     * Extract token from Authorization header (Bearer token)
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Validate JWT token
     */
    private boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract claims from JWT token
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
