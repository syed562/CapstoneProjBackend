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
    
    /**
     * Deactivate a user account
     * @param userId the user ID to deactivate
     */
    public void deactivateUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_INACTIVE);
            userRepository.save(user.get());
        }
    }
    
    /**
     * Activate a previously deactivated user account
     * @param userId the user ID to activate
     */
    public void activateUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_ACTIVE);
            userRepository.save(user.get());
        }
    }
    
    /**
     * Suspend a user account (for violations, security issues, etc.)
     * @param userId the user ID to suspend
     */
    public void suspendUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setStatus(User.STATUS_SUSPENDED);
            userRepository.save(user.get());
        }
    }
    
    /**
     * Check if a user is active
     * @param userId the user ID to check
     * @return true if user is active, false otherwise
     */
    public boolean isUserActive(String userId) {
        return userRepository.findById(userId)
            .map(User::isActive)
            .orElse(false);
    }
    
    /**
     * Get the status of a user
     * @param userId the user ID
     * @return the user status string
     */
    public String getUserStatus(String userId) {
        return userRepository.findById(userId)
            .map(User::getStatus)
            .orElse(null);
    }
}
