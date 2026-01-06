package com.example.loanapplication.client;

import com.example.loanapplication.client.dto.UserView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {
    
    @Override
    public UserView getUser(String userId) {
        // Return a fallback user with only the userId set
        log.warn("UserServiceClient fallback invoked for userId: {}", userId);
        UserView fallback = new UserView();
        fallback.setId(userId);
        fallback.setUsername("Unknown");
        return fallback;
    }
}

