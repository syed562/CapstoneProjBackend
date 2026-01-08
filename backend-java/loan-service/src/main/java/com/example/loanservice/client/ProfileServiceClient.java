package com.example.loanservice.client;

import com.example.loanservice.client.dto.ProfileView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "http://localhost:8082", fallback = ProfileServiceClientFallback.class)
public interface ProfileServiceClient {
    
    @GetMapping("/api/profiles/{userId}")
    ProfileView getProfile(@PathVariable("userId") String userId);
}
