package com.example.loanservice.client;

import com.example.loanservice.client.dto.ProfileView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProfileServiceClientFallback implements ProfileServiceClient {
    
    @Override
    public ProfileView getProfile(String userId) {
        log.warn("ProfileServiceClient fallback invoked for userId: {}", userId);
        return null;
    }
}
