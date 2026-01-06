package com.example.authservice.controller;

import com.example.authservice.controller.dto.LoginRequest;
import com.example.authservice.controller.dto.LoginResponse;
import com.example.authservice.controller.dto.RegisterRequest;
import com.example.authservice.controller.dto.RegisterResponse;
import com.example.authservice.controller.dto.ChangePasswordRequest;
import com.example.authservice.domain.User;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login attempt for username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        log.debug("Login successful for user: {}, role: {}", response.getUsername(), response.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received: {}", request.getUsername());
        log.debug("Registration details - username: {}, email: {}, role: {}", 
            request.getUsername(), request.getEmail(), request.getRole());
        RegisterResponse response = authService.register(request);
        log.debug("Registration successful for user: {}, userId: {}", response.getUsername(), response.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        log.debug("Fetching user details for userId: {}", userId);
        User user = authService.getUserById(userId);
        log.debug("User found: {}, role: {}, status: {}", user.getUsername(), user.getRole(), user.getStatus());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        log.debug("Admin request to list all users");
        requireAdmin();
        List<User> users = authService.listUsers();
        log.debug("Retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        log.debug("Admin request to update role for userId: {}", userId);
        requireAdmin();
        String role = body.get("role");
        log.debug("Updating user {} role to: {}", userId, role);
        User updatedUser = authService.updateUserRole(userId, role);
        log.debug("Role updated successfully for user: {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> updateStatus(@PathVariable String userId, @RequestBody Map<String, String> body) {
        log.debug("Admin request to update status for userId: {}", userId);
        requireAdmin();
        String status = body.get("status");
        log.debug("Updating user {} status to: {}", userId, status);
        User updatedUser = authService.updateUserStatus(userId, status);
        log.debug("Status updated successfully for user: {}", updatedUser.getUsername());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        log.debug("Admin request to delete userId: {}", userId);
        requireAdmin();
        // Prevent self-deletion
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && userId.equals(auth.getPrincipal())) {
            log.warn("Attempt to delete own account by userId: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete your own account");
        }
        authService.deleteUser(userId);
        log.debug("User deleted successfully: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            log.warn("Unauthenticated change password attempt");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        String userId = auth.getPrincipal().toString();
        log.debug("Password change request for userId: {}", userId);
        authService.changePassword(userId, request);
        log.debug("Password changed successfully for userId: {}", userId);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
    }
}
