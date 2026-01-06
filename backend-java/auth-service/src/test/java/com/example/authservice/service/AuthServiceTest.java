package com.example.authservice.service;

import com.example.authservice.controller.dto.LoginRequest;
import com.example.authservice.controller.dto.LoginResponse;
import com.example.authservice.controller.dto.RegisterRequest;
import com.example.authservice.controller.dto.RegisterResponse;
import com.example.authservice.domain.User;
import com.example.authservice.domain.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.controller.dto.ChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        User user = new User();
        user.setId("user123");
        user.setUsername("john");
        user.setPassword("$2a$10$encodedPassword");
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("user123", "john", "CUSTOMER")).thenReturn("jwt-token-xyz");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-xyz", response.getToken());
        assertEquals("user123", response.getUserId());
        assertEquals("john", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
        verify(userRepository, times(1)).findByUsername("john");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw 401 when username not found")
    void testLoginUsernameNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("Should throw 401 when password is incorrect")
    void testLoginWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("TestPass123!");

        User user = new User();
        user.setId("user123");
        user.setUsername("john");
        user.setPassword("$2a$10$encodedPassword");
        user.setStatus("ACTIVE");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPass123!", "$2a$10$encodedPassword")).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw 403 when user account is not active")
    void testLoginInactiveUser() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("TestPass123!");

        User user = new User();
        user.setId("user123");
        user.setUsername("john");
        user.setPassword("$2a$10$encodedPassword");
        user.setStatus("SUSPENDED");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("TestPass123!", "$2a$10$encodedPassword")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.login(request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertTrue(exception.getReason().contains("not active"));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("SecurePass123!");
        request.setRole("CUSTOMER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw 409 when username already exists")
    void testRegisterDuplicateUsername() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("SecurePass123!");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Username already exists"));
    }

    @Test
    @DisplayName("Should throw 409 when email already exists")
    void testRegisterDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("SecurePass123!");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Email already exists"));
    }

    @Test
    @DisplayName("Should default to CUSTOMER role if role not provided")
    void testRegisterDefaultRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("SecurePass123!");
        request.setRole(null); // No role provided

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserByIdSuccess() {
        // Arrange
        String userId = "user123";
        User user = new User();
        user.setId(userId);
        user.setUsername("john");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = authService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("john", result.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw 404 when user not found by ID")
    void testGetUserByIdNotFound() {
        // Arrange
        String userId = "nonexistent";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.getUserById(userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found"));
    }

    @Test
    @DisplayName("Should encode password during registration")
    void testPasswordEncodingDuringRegistration() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("secureuser");
        request.setEmail("secure@example.com");
        request.setPassword("SecurePass123!");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("$2a$10$encodedSecurePassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder, times(1)).encode("SecurePass123!");
        verify(userRepository, times(1)).save(argThat(user -> 
            "$2a$10$encodedSecurePassword".equals(user.getPassword())
        ));
    }

    @Test
    @DisplayName("Should set user status to ACTIVE on registration")
    void testUserStatusActiveOnRegistration() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("SecurePass123!");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> 
            "ACTIVE".equals(user.getStatus())
        ));
    }

    @Test
    @DisplayName("Should update user role to uppercase")
    void testUpdateUserRole() {
        User user = new User();
        user.setId("u1");
        user.setRole("customer");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = authService.updateUserRole("u1", "loan_officer");

        assertEquals("LOAN_OFFICER", updated.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw 400 when role missing")
    void testUpdateUserRoleMissing() {
        assertThrows(ResponseStatusException.class, () -> authService.updateUserRole("u1", ""));
    }

    @Test
    @DisplayName("Should update user status to uppercase")
    void testUpdateUserStatus() {
        User user = new User();
        user.setId("u1");
        user.setStatus("pending");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = authService.updateUserStatus("u1", "suspended");

        assertEquals("SUSPENDED", updated.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw 400 when status missing")
    void testUpdateUserStatusMissing() {
        assertThrows(ResponseStatusException.class, () -> authService.updateUserStatus("u1", " "));
    }

    @Test
    @DisplayName("Should delete existing user")
    void testDeleteUser() {
        when(userRepository.existsById("u1")).thenReturn(true);

        authService.deleteUser("u1");

        verify(userRepository).deleteById("u1");
    }

    @Test
    @DisplayName("Should throw 404 when deleting non-existent user")
    void testDeleteUserNotFound() {
        when(userRepository.existsById("u1")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> authService.deleteUser("u1"));
    }

    @Test
    @DisplayName("Should list users")
    void testListUsers() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(new User()));
        assertEquals(1, authService.listUsers().size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should change password with valid data")
    void testChangePasswordSuccess() {
        User user = new User();
        user.setId("u1");
        user.setUsername("john");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.matches("NewPass@123", "encodedOld")).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encodedNew");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("NewPass@123");
        req.setConfirmPassword("NewPass@123");

        authService.changePassword("u1", req);

        verify(userRepository).save(argThat(u -> "encodedNew".equals(u.getPassword())));
    }

    @Test
    @DisplayName("Change password should reject wrong current password")
    void testChangePasswordWrongCurrent() {
        User user = new User();
        user.setId("u1");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("new-pass");
        req.setConfirmPassword("new-pass");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("u1", req));
    }

    @Test
    @DisplayName("Change password should reject same as current")
    void testChangePasswordSameAsCurrent() {
        User user = new User();
        user.setId("u1");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("old");
        req.setConfirmPassword("old");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("u1", req));
    }

    @Test
    @DisplayName("Change password should reject mismatch confirmation")
    void testChangePasswordMismatch() {
        User user = new User();
        user.setId("u1");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.matches("new-pass", "encodedOld")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("new-pass");
        req.setConfirmPassword("different");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("u1", req));
    }

    @Test
    @DisplayName("Change password should reject blank new password")
    void testChangePasswordMissingNewPassword() {
        User user = new User();
        user.setId("u1");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword(" ");
        req.setConfirmPassword(" ");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("u1", req));
    }

    @Test
    @DisplayName("Change password should reject weak new password")
    void testChangePasswordWeakPassword() {
        User user = new User();
        user.setId("u1");
        user.setPassword("encodedOld");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.matches("weak", "encodedOld")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("weak");
        req.setConfirmPassword("weak");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("u1", req));
    }

    @Test
    @DisplayName("Change password should return 404 when user missing")
    void testChangePasswordUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("Newpass123!");
        req.setConfirmPassword("Newpass123!");

        assertThrows(ResponseStatusException.class, () -> authService.changePassword("missing", req));
    }
}
