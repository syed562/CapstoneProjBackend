package com.example.loanapplication.client;

import com.example.loanapplication.client.dto.ProfileView;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class ProfileServiceClientFallback implements ProfileServiceClient {

    @Override
    public ProfileView getProfile(String userId) {
        log.warn("⚠️ FALLBACK: Profile Service is unavailable for userId: {}", userId);
        log.warn("⚠️ FALLBACK: Returning null profile - loan application will proceed without profile validation");
      
        return null;
    }
}
