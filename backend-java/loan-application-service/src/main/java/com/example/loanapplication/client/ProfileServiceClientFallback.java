package com.example.loanapplication.client;

import com.example.loanapplication.client.dto.ProfileView;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback handler for ProfileServiceClient
 * Called when profile-service is unavailable
 */
@Component
@Slf4j
public class ProfileServiceClientFallback implements ProfileServiceClient {

    @Override
    public ProfileView getProfile(String userId) {
        log.warn("⚠️ FALLBACK: Profile Service is unavailable for userId: {}", userId);
        log.warn("⚠️ FALLBACK: Returning null profile - loan application will proceed without profile validation");
        
        // Return null to allow application to continue
        // The service layer already handles null profiles gracefully
        return null;
    }
}
