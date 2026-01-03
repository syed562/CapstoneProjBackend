package com.example.profileservice.service;

import com.example.profileservice.controller.dto.UpdateKycStatusRequest;
import com.example.profileservice.controller.dto.UpdateProfileRequest;
import com.example.profileservice.domain.Profile;
import com.example.profileservice.domain.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(profileRepository);
    }

    @Test
    @DisplayName("Should get profile by user ID")
    void testGetProfileSuccess() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFirstName("John");
        profile.setLastName("Doe");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act
        Profile result = profileService.getOwn(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John", result.getFirstName());
        verify(profileRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw 404 when profile not found")
    void testGetProfileNotFound() {
        // Arrange
        String userId = "nonexistent";
        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                profileService.getOwn(userId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Profile not found"));
    }

    @Test
    @DisplayName("Should create new profile if not exists")
    void testUpdateProfileCreatesNewProfile() {
        // Arrange
        String userId = "newuser";
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPhone("1234567890");
        request.setAddressLine1("123 Main St");
        request.setCity("NYC");
        request.setState("NY");
        request.setPostalCode("10001");
        request.setCountry("USA");
        request.setCreditScore(750.0);
        request.setAnnualIncome(80000.0);
        request.setTotalLiabilities(5000.0);

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateOwn(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("PENDING", result.getKycStatus());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should update existing profile")
    void testUpdateExistingProfile() {
        // Arrange
        String userId = "user123";
        Profile existingProfile = new Profile();
        existingProfile.setUserId(userId);
        existingProfile.setFirstName("OldName");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("NewName");
        request.setLastName("Doe");
        request.setEmail("new@example.com");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateOwn(userId, request);

        // Assert
        assertEquals("NewName", result.getFirstName());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should update KYC status to APPROVED")
    void testUpdateKycStatusApproved() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setKycStatus("PENDING");

        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("APPROVED");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateKyc(userId, request);

        // Assert
        assertEquals("APPROVED", result.getKycStatus());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should update KYC status to REJECTED")
    void testUpdateKycStatusRejected() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setKycStatus("PENDING");

        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("REJECTED");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateKyc(userId, request);

        // Assert
        assertEquals("REJECTED", result.getKycStatus());
    }

    @Test
    @DisplayName("Should throw 400 for invalid KYC status")
    void testUpdateKycInvalidStatus() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);

        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("INVALID_STATUS");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                profileService.updateKyc(userId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("PENDING, APPROVED, or REJECTED"));
    }

    @Test
    @DisplayName("Should throw 400 when KYC status is blank")
    void testUpdateKycBlankStatus() {
        // Arrange
        String userId = "user123";
        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                profileService.updateKyc(userId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("kycStatus is required"));
    }

    @Test
    @DisplayName("Should throw 404 when updating KYC for nonexistent profile")
    void testUpdateKycProfileNotFound() {
        // Arrange
        String userId = "nonexistent";
        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("APPROVED");

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                profileService.updateKyc(userId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should normalize KYC status to uppercase")
    void testKycStatusNormalization() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);

        UpdateKycStatusRequest request = new UpdateKycStatusRequest();
        request.setKycStatus("approved");  // lowercase

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateKyc(userId, request);

        // Assert
        assertEquals("APPROVED", result.getKycStatus());  // Should be uppercase
    }

    @Test
    @DisplayName("Should set default KYC status to PENDING on create")
    void testDefaultKycStatus() {
        // Arrange
        String userId = "newuser";
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateOwn(userId, request);

        // Assert
        assertEquals("PENDING", result.getKycStatus());
    }

    @Test
    @DisplayName("Should preserve credit score when provided")
    void testUpdateWithCreditScore() {
        // Arrange
        String userId = "user123";
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setCreditScore(800.0);

        when(profileRepository.findById(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Profile result = profileService.updateOwn(userId, request);

        // Assert
        assertEquals("800.0", result.getCreditScore());
    }

    @Test
    @DisplayName("Should get profile by user ID using alternative method")
    void testGetByUserId() {
        // Arrange
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act
        Profile result = profileService.getByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }
}
