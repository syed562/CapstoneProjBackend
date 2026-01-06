package com.example.loanapplication.client;

import com.example.loanapplication.client.dto.UserView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/api/auth/users/{userId}")
    UserView getUser(@PathVariable("userId") String userId);
}
