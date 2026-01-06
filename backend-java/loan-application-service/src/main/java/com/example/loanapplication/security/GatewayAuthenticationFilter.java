package com.example.loanapplication.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to extract authentication from gateway headers (X-User-Id, X-User-Role, X-User-Name)
 * Gateway validates JWT and passes user info downstream
 */
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String username = request.getHeader("X-User-Name");

        log.debug("[GATEWAY-FILTER] Request URI: {}", request.getRequestURI());
        log.debug("[GATEWAY-FILTER] X-User-Id: {}, X-User-Role: {}, X-User-Name: {}", userId, role, username);

        if (userId != null && role != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(authority)
                    );
            authentication.setDetails(username);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("[GATEWAY-FILTER] Authentication set for user {} with role {}", userId, role);
        } else {
            log.warn("[GATEWAY-FILTER] Missing gateway headers - userId: {}, role: {}", userId, role);
        }

        filterChain.doFilter(request, response);
    }
}
