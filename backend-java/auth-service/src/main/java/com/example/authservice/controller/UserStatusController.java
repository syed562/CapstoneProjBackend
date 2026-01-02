package com.example.authservice.controller;

import com.example.authservice.service.UserStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserStatusController {
    
    private final UserStatusService userStatusService;
    
    public UserStatusController(UserStatusService userStatusService) {
        this.userStatusService = userStatusService;
    }
    
    /**
     * Deactivate a user account
     * Only ADMIN or the user themselves can deactivate
     * @param userId the user ID to deactivate
     * @return success message
     */
    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<String> deactivateUser(@PathVariable("userId") String userId) {
        userStatusService.deactivateUser(userId);
        return ResponseEntity.ok("User deactivated successfully");
    }
    
    /**
     * Activate a user account
     * Only ADMIN can activate users
     * @param userId the user ID to activate
     * @return success message
     */
    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(@PathVariable("userId") String userId) {
        userStatusService.activateUser(userId);
        return ResponseEntity.ok("User activated successfully");
    }
    
    /**
     * Suspend a user account
     * Only ADMIN can suspend users
     * @param userId the user ID to suspend
     * @return success message
     */
    @PutMapping("/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> suspendUser(@PathVariable("userId") String userId) {
        userStatusService.suspendUser(userId);
        return ResponseEntity.ok("User suspended successfully");
    }
    
    /**
     * Get the status of a user
     * @param userId the user ID
     * @return the user status
     */
    @GetMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<String> getUserStatus(@PathVariable("userId") String userId) {
        String status = userStatusService.getUserStatus(userId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}
