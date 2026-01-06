package com.example.authservice.service;

import com.example.authservice.controller.dto.LoginRequest;
import com.example.authservice.controller.dto.LoginResponse;
import com.example.authservice.controller.dto.RegisterRequest;
import com.example.authservice.controller.dto.RegisterResponse;
import com.example.authservice.controller.dto.ChangePasswordRequest;
import com.example.authservice.domain.User;
import com.example.authservice.domain.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for username: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getUsername());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
                });

        log.debug("User found: {}, checking password", user.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        log.debug("Password verified for user: {}, checking status: {}", user.getUsername(), user.getStatus());
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("Login failed - user {} has non-active status: {}", user.getUsername(), user.getStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        log.debug("Generating JWT token for user: {}, role: {}", user.getUsername(), user.getRole());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.debug("Login successful for user: {}", user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public RegisterResponse register(RegisterRequest request) {
        log.debug("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());
        
        // Validate password strength
        log.debug("Validating password strength for user: {}", request.getUsername());
        if (!com.example.authservice.security.PasswordValidator.isStrong(request.getPassword())) {
            log.warn("Registration failed - weak password for user: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                com.example.authservice.security.PasswordValidator.getStrengthMessage(request.getPassword()));
        }

        // Check if username already exists
        log.debug("Checking if username exists: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Check if email already exists
        log.debug("Checking if email exists: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "CUSTOMER");
        user.setStatus("ACTIVE");
        String now = Instant.now().toString();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        log.debug("Saving new user: {}, role: {}", user.getUsername(), user.getRole());
        userRepository.save(user);
        log.debug("User registered successfully: {}, userId: {}", user.getUsername(), user.getId());

        return new RegisterResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public User getUserById(String userId) {
        log.debug("Fetching user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    public void deleteUser(String userId) {
        log.debug("Attempting to delete user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("Delete failed - user not found with id: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(userId);
        log.debug("User deleted successfully: {}", userId);
    }

    public List<User> listUsers() {
        log.debug("Fetching all users from database");
        List<User> users = userRepository.findAll();
        log.debug("Retrieved {} users", users.size());
        return users;
    }

    public User updateUserRole(String userId, String role) {
        log.debug("Updating role for userId: {} to role: {}", userId, role);
        if (role == null || role.isBlank()) {
            log.warn("Update role failed - role is required for userId: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        User user = getUserById(userId);
        String oldRole = user.getRole();
        user.setRole(role.toUpperCase());
        user.setUpdatedAt(Instant.now().toString());
        User updatedUser = userRepository.save(user);
        log.debug("Role updated successfully for user: {} from {} to {}", user.getUsername(), oldRole, role);
        return updatedUser;
    }

    public User updateUserStatus(String userId, String status) {
        log.debug("Updating status for userId: {} to status: {}", userId, status);
        if (status == null || status.isBlank()) {
            log.warn("Update status failed - status is required for userId: {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        User user = getUserById(userId);
        String oldStatus = user.getStatus();
        user.setStatus(status.toUpperCase());
        user.setUpdatedAt(Instant.now().toString());
        User updatedUser = userRepository.save(user);
        log.debug("Status updated successfully for user: {} from {} to {}", user.getUsername(), oldStatus, status);
        return updatedUser;
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        log.debug("Password change attempt for userId: {}", userId);
        User user = getUserById(userId);

        log.debug("Verifying current password for user: {}", user.getUsername());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed - incorrect current password for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            log.warn("Password change failed - new password is required for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }

        if (request.getConfirmPassword() != null && !request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed - password mismatch for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed - new password same as current for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        // Validate new password strength
        log.debug("Validating new password strength for user: {}", user.getUsername());
        if (!com.example.authservice.security.PasswordValidator.isStrong(request.getNewPassword())) {
            log.warn("Password change failed - weak new password for user: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                com.example.authservice.security.PasswordValidator.getStrengthMessage(request.getNewPassword()));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now().toString());
        userRepository.save(user);
        log.debug("Password changed successfully for user: {}", user.getUsername());
    }
}
