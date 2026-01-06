package com.example.authservice.controller;

import com.example.authservice.controller.dto.LoginRequest;
import com.example.authservice.controller.dto.LoginResponse;
import com.example.authservice.controller.dto.RegisterRequest;
import com.example.authservice.controller.dto.RegisterResponse;
import com.example.authservice.controller.dto.ChangePasswordRequest;
import com.example.authservice.domain.User;
import com.example.authservice.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
    @DisplayName("Admin can list all users")
    void testListUsersAsAdmin() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        User u = new User();
        u.setId("u1");
        when(authService.listUsers()).thenReturn(List.of(u));

        // Act
        ResponseEntity<List<User>> response = authController.listUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(authService).listUsers();
    }

    @Test
    @DisplayName("Non-admin cannot list users")
    void testListUsersAsNonAdminForbidden() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
        );

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> authController.listUsers());
        verify(authService, never()).listUsers();
    }

    @Test
    @DisplayName("Admin can update role")
    void testUpdateRoleAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        User updated = new User();
        updated.setRole("LOAN_OFFICER");
        when(authService.updateUserRole("u1", "LOAN_OFFICER")).thenReturn(updated);

        ResponseEntity<User> response = authController.updateRole("u1", Map.of("role", "LOAN_OFFICER"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("LOAN_OFFICER", response.getBody().getRole());
        verify(authService).updateUserRole("u1", "LOAN_OFFICER");
    }

    @Test
    @DisplayName("Admin can update status")
    void testUpdateStatusAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        User updated = new User();
        updated.setStatus("SUSPENDED");
        when(authService.updateUserStatus("u1", "SUSPENDED")).thenReturn(updated);

        ResponseEntity<User> response = authController.updateStatus("u1", Map.of("status", "SUSPENDED"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUSPENDED", response.getBody().getStatus());
        verify(authService).updateUserStatus("u1", "SUSPENDED");
    }

    @Test
    @DisplayName("Admin can delete user")
    void testDeleteUserAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        ResponseEntity<Void> response = authController.deleteUser("target-id");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authService).deleteUser("target-id");
    }

    @Test
    @DisplayName("User cannot delete self")
    void testDeleteUserSelfForbidden() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("self-id", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        assertThrows(ResponseStatusException.class, () -> authController.deleteUser("self-id"));
        verify(authService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Change password requires authentication")
    void testChangePasswordAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user-123", null,
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
        );

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("new");

        ResponseEntity<Map<String, String>> response = authController.changePassword(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody().get("message"));
        verify(authService).changePassword("user-123", req);
    }

    @Test
    @DisplayName("Change password unauthenticated should throw 401")
    void testChangePasswordUnauthenticated() {
        SecurityContextHolder.clearContext();
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("new");

        assertThrows(ResponseStatusException.class, () -> authController.changePassword(req));
        verify(authService, never()).changePassword(anyString(), any());
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
