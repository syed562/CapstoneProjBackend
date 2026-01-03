package com.example.authservice.controller;

import com.example.authservice.service.UserStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserStatusController Tests")
class UserStatusControllerTest {

    @Mock
    private UserStatusService userStatusService;

    private UserStatusController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserStatusController(userStatusService);
    }

    @Test
    @DisplayName("Should deactivate user and return 200")
    void deactivateUser_shouldReturnOk() {
        String userId = "user123";

        ResponseEntity<String> response = controller.deactivateUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deactivated successfully", response.getBody());
        verify(userStatusService, times(1)).deactivateUser(userId);
    }

    @Test
    @DisplayName("Should activate user and return 200")
    void activateUser_shouldReturnOk() {
        String userId = "user456";

        ResponseEntity<String> response = controller.activateUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User activated successfully", response.getBody());
        verify(userStatusService, times(1)).activateUser(userId);
    }

    @Test
    @DisplayName("Should suspend user and return 200")
    void suspendUser_shouldReturnOk() {
        String userId = "user789";

        ResponseEntity<String> response = controller.suspendUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User suspended successfully", response.getBody());
        verify(userStatusService, times(1)).suspendUser(userId);
    }

    @Test
    @DisplayName("Should return user status when found")
    void getUserStatus_found() {
        String userId = "userActive";
        when(userStatusService.getUserStatus(userId)).thenReturn("ACTIVE");

        ResponseEntity<String> response = controller.getUserStatus(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ACTIVE", response.getBody());
        verify(userStatusService, times(1)).getUserStatus(userId);
    }

    @Test
    @DisplayName("Should return 404 when user status not found")
    void getUserStatus_notFound() {
        String userId = "missing";
        when(userStatusService.getUserStatus(userId)).thenReturn(null);

        ResponseEntity<String> response = controller.getUserStatus(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userStatusService, times(1)).getUserStatus(userId);
    }
}
