package com.example.authservice.controller;

import com.example.authservice.controller.dto.LoginRequest;
import com.example.authservice.controller.dto.LoginResponse;
import com.example.authservice.controller.dto.RegisterRequest;
import com.example.authservice.controller.dto.RegisterResponse;
import com.example.authservice.domain.User;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authService);
    }

    @Test
    @DisplayName("Should login successfully and return token")
    void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        LoginResponse loginResponse = new LoginResponse(
                "jwt-token-xyz",
                "user123",
                "john",
                "CUSTOMER"
        );

        when(authService.login(request)).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-xyz", response.getBody().getToken());
        assertEquals("john", response.getBody().getUsername());
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should fail login with invalid username")
    void testLoginFailureInvalidUsername() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(authService.login(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.login(request));
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void testLoginFailureInvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrongpassword");

        when(authService.login(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.login(request));
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should fail login with inactive user account")
    void testLoginFailureInactiveAccount() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        when(authService.login(request))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.login(request));
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should register new user and return 201")
    void testRegisterSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        RegisterResponse registerResponse = new RegisterResponse(
                "user123",
                "newuser",
                "new@example.com",
                "CUSTOMER"
        );

        when(authService.register(request)).thenReturn(registerResponse);

        // Act
        ResponseEntity<RegisterResponse> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("CUSTOMER", response.getBody().getRole());
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should fail register with duplicate username")
    void testRegisterFailureDuplicateUsername() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(authService.register(request))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.register(request));
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should fail register with duplicate email")
    void testRegisterFailureDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(authService.register(request))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.register(request));
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should register with custom role")
    void testRegisterWithCustomRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("adminuser");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        RegisterResponse registerResponse = new RegisterResponse(
                "user456",
                "adminuser",
                "admin@example.com",
                "ADMIN"
        );

        when(authService.register(request)).thenReturn(registerResponse);

        // Act
        ResponseEntity<RegisterResponse> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ADMIN", response.getBody().getRole());
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById() {
        // Arrange
        String userId = "user123";
        User user = new User();
        user.setId(userId);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");

        when(authService.getUserById(userId)).thenReturn(user);

        // Act
        ResponseEntity<User> response = authController.getUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("john", response.getBody().getUsername());
        assertEquals("john@example.com", response.getBody().getEmail());
        verify(authService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should fail get user with non-existent ID")
    void testGetUserByIdNotFound() {
        // Arrange
        String userId = "nonexistent";

        when(authService.getUserById(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.getUser(userId));
        verify(authService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should return user with all details")
    void testGetUserByIdWithAllDetails() {
        // Arrange
        String userId = "user456";
        User user = new User();
        user.setId(userId);
        user.setUsername("jane");
        user.setEmail("jane@example.com");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        user.setCreatedAt("2025-01-01T00:00:00Z");
        user.setUpdatedAt("2025-01-02T00:00:00Z");

        when(authService.getUserById(userId)).thenReturn(user);

        // Act
        ResponseEntity<User> response = authController.getUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jane", response.getBody().getUsername());
        assertEquals("ADMIN", response.getBody().getRole());
        assertEquals("ACTIVE", response.getBody().getStatus());
        verify(authService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should handle login request with empty password")
    void testLoginWithEmptyPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("");

        when(authService.login(request))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.login(request));
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should handle register request with valid data and default role")
    void testRegisterWithDefaultRole() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("defaultroleuser");
        request.setEmail("defaultrole@example.com");
        request.setPassword("password123");
        // Role is null, should default to CUSTOMER

        RegisterResponse registerResponse = new RegisterResponse(
                "user789",
                "defaultroleuser",
                "defaultrole@example.com",
                "CUSTOMER"
        );

        when(authService.register(request)).thenReturn(registerResponse);

        // Act
        ResponseEntity<RegisterResponse> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CUSTOMER", response.getBody().getRole());
        verify(authService, times(1)).register(request);
    }
}
