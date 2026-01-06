package com.example.reportservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Feign interceptor to forward authentication headers to downstream services
 * Extracts user info from SecurityContext and adds X-User-* headers
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            String userId = auth.getPrincipal().toString();
            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("CUSTOMER");
            String username = auth.getDetails() != null ? auth.getDetails().toString() : userId;
            
            // Add authentication headers for downstream services
            template.header("X-User-Id", userId);
            template.header("X-User-Role", role);
            template.header("X-User-Name", username);
        }
    }
}
