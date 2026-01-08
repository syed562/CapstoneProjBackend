package com.example.authservice.service;

import com.example.authservice.domain.User;
import com.example.authservice.domain.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserStatusService {
    
    private final UserRepository userRepository;
    
    public UserStatusService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    
    public void deactivateUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_INACTIVE);
            userRepository.save(user.get());
        }
    }
    
   
    public void activateUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_ACTIVE);
            userRepository.save(user.get());
        }
    }
    
    public void suspendUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_SUSPENDED);
            userRepository.save(user.get());
        }
    }
    
   
    public boolean isUserActive(String userId) {
        return userRepository.findById(userId)
            .map(User::isActive)
            .orElse(false);
    }
    
    
    public String getUserStatus(String userId) {
        return userRepository.findById(userId)
            .map(User::getStatus)
            .orElse(null);
    }
}
