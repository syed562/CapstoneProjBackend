package com.example.profileservice.controller;

import com.example.profileservice.controller.dto.UpdateKycStatusRequest;
import com.example.profileservice.controller.dto.UpdateProfileRequest;
import com.example.profileservice.domain.Profile;
import com.example.profileservice.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private Profile mockProfile() {
        Profile p = new Profile();
        p.setUserId("user1");
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setEmail("john@example.com");
        p.setKycStatus("PENDING");
        return p;
    }

    private UpdateProfileRequest validProfileRequest() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setCity("Hyderabad");
        req.setCreditScore(750.0);
        return req;
    }

    // ------------------- GET /me -------------------

    @Test
    void getOwnProfile_success() throws Exception {
        when(profileService.getOwn("user1")).thenReturn(mockProfile());

        mockMvc.perform(get("/api/profiles/me")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    void getOwnProfile_missingUserId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/profiles/me")
                        .param("userId", ""))
                .andExpect(status().isBadRequest());
    }

    // ------------------- PUT /me -------------------

    @Test
    void updateOwnProfile_success() throws Exception {
        when(profileService.updateOwn(eq("user1"), any(UpdateProfileRequest.class)))
                .thenReturn(mockProfile());

        mockMvc.perform(put("/api/profiles/me")
                        .param("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProfileRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void updateOwnProfile_validationFailure_shouldReturn400() throws Exception {
        UpdateProfileRequest invalid = new UpdateProfileRequest(); // missing required fields

        mockMvc.perform(put("/api/profiles/me")
                        .param("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ------------------- GET /{userId} -------------------

    @Test
    void getProfileByUserId_success() throws Exception {
        when(profileService.getByUserId("user1")).thenReturn(mockProfile());

        mockMvc.perform(get("/api/profiles/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    // ------------------- PUT /{userId}/kyc -------------------

    @Test
    void updateKyc_success() throws Exception {
        UpdateKycStatusRequest req = new UpdateKycStatusRequest();
        req.setKycStatus("APPROVED");

        Profile updated = mockProfile();
        updated.setKycStatus("APPROVED");

        when(profileService.updateKyc(eq("user1"), any(UpdateKycStatusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/profiles/user1/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kycStatus").value("APPROVED"));
    }

    @Test
    void updateKyc_invalidValue_shouldReturn400() throws Exception {
        UpdateKycStatusRequest req = new UpdateKycStatusRequest();
        req.setKycStatus(""); // invalid

        mockMvc.perform(put("/api/profiles/user1/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
