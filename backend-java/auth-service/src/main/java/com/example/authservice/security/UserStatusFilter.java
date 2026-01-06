package com.example.authservice.security;

import com.example.authservice.domain.User;
import com.example.authservice.domain.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filter to prevent inactive and suspended users from accessing the API
 * This filter checks the user status before allowing requests to proceed
 */
@Slf4j
@Component
public class UserStatusFilter extends OncePerRequestFilter {
    
    private final UserRepository userRepository;
    
    // Endpoints that should be excluded from user status check
    private static final String[] EXCLUDED_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh-token",
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs"
    };
    
    public UserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Check if the request path should be excluded
        String requestPath = request.getRequestURI();
        log.debug("UserStatusFilter processing request: {}", requestPath);
        
        if (isExcludedPath(requestPath)) {
            log.debug("Path excluded from user status check: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get user ID from request header or authentication context
        String userId = request.getHeader("X-User-Id");
        log.debug("Checking user status for userId: {}", userId);
        
        if (userId != null && !userId.isEmpty()) {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user != null) {
                log.debug("User found: {}, status: {}, isActive: {}", user.getUsername(), user.getStatus(), user.isActive());
                if (!user.isActive()) {
                    // Return 403 Forbidden for inactive users
                    log.warn("Access denied for inactive user: {}, status: {}", user.getUsername(), user.getStatus());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write(String.format(
                        "{\"error\": \"User account is %s\", \"status\": \"%s\"}", 
                        user.getStatus().toLowerCase(), user.getStatus()
                    ));
                    return;
                }
            } else {
                log.debug("User not found for userId: {}", userId);
            }
        } else {
            log.debug("No userId header found in request");
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the request path should be excluded from user status validation
     */
    private boolean isExcludedPath(String path) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.contains(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
