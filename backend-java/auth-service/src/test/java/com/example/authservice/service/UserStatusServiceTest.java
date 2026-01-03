package com.example.authservice.service;

import com.example.authservice.domain.User;
import com.example.authservice.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("UserStatusService Tests")
class UserStatusServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserStatusService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserStatusService(userRepository);
    }

    @Test
    @DisplayName("Deactivate user when found")
    void deactivateUser_found_updatesStatus() {
        User user = new User();
        user.setId("u1");
        user.setStatus(User.STATUS_ACTIVE);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        service.deactivateUser("u1");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(User.STATUS_INACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deactivate user when missing does nothing")
    void deactivateUser_missing_noSave() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        service.deactivateUser("missing");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Activate user when found")
    void activateUser_found_updatesStatus() {
        User user = new User();
        user.setId("u2");
        user.setStatus(User.STATUS_INACTIVE);
        when(userRepository.findById("u2")).thenReturn(Optional.of(user));

        service.activateUser("u2");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(User.STATUS_ACTIVE, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Suspend user when found")
    void suspendUser_found_updatesStatus() {
        User user = new User();
        user.setId("u3");
        user.setStatus(User.STATUS_ACTIVE);
        when(userRepository.findById("u3")).thenReturn(Optional.of(user));

        service.suspendUser("u3");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(User.STATUS_SUSPENDED, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("isUserActive returns true for active user")
    void isUserActive_active() {
        User user = new User();
        user.setStatus(User.STATUS_ACTIVE);
        when(userRepository.findById("u4")).thenReturn(Optional.of(user));

        assertTrue(service.isUserActive("u4"));
    }

    @Test
    @DisplayName("isUserActive returns false for inactive user")
    void isUserActive_inactive() {
        User user = new User();
        user.setStatus(User.STATUS_INACTIVE);
        when(userRepository.findById("u5")).thenReturn(Optional.of(user));

        assertFalse(service.isUserActive("u5"));
    }

    @Test
    @DisplayName("isUserActive returns false when user missing")
    void isUserActive_missing() {
        when(userRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertFalse(service.isUserActive("missing"));
    }

    @Test
    @DisplayName("getUserStatus returns status when found")
    void getUserStatus_found() {
        User user = new User();
        user.setStatus(User.STATUS_ACTIVE);
        when(userRepository.findById("u6")).thenReturn(Optional.of(user));

        assertEquals(User.STATUS_ACTIVE, service.getUserStatus("u6"));
    }

    @Test
    @DisplayName("getUserStatus returns null when missing")
    void getUserStatus_missing() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertNull(service.getUserStatus("missing"));
    }
}
