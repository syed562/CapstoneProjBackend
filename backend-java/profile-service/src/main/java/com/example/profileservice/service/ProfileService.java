package com.example.profileservice.service;

import com.example.profileservice.controller.dto.UpdateKycStatusRequest;
import com.example.profileservice.controller.dto.UpdateProfileRequest;
import com.example.profileservice.domain.Profile;
import com.example.profileservice.domain.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

@Service
public class ProfileService {
    private static final Set<String> ALLOWED_KYC = Set.of("PENDING", "APPROVED", "REJECTED");

    private final ProfileRepository repo;

    public ProfileService(ProfileRepository repo) {
        this.repo = repo;
    }

    public Profile getOwn(String userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public Profile updateOwn(String userId, UpdateProfileRequest req) {
        String now = Instant.now().toString();
        Profile profile = repo.findById(userId).orElseGet(() -> {
            Profile p = new Profile();
            p.setUserId(userId);
            p.setCreatedAt(now);
            return p;
        });

        profile.setFirstName(req.getFirstName());
        profile.setLastName(req.getLastName());
        profile.setEmail(req.getEmail());
        profile.setPhone(req.getPhone());
        profile.setAddressLine1(req.getAddressLine1());
        profile.setAddressLine2(req.getAddressLine2());
        profile.setCity(req.getCity());
        profile.setState(req.getState());
        profile.setPostalCode(req.getPostalCode());
        profile.setCountry(req.getCountry());
        if (profile.getKycStatus() == null || profile.getKycStatus().isBlank()) {
            profile.setKycStatus("PENDING");
        }
        profile.setUpdatedAt(now);
        return repo.save(profile);
    }

    public Profile getByUserId(String userId) {
        return getOwn(userId);
    }

    public Profile updateKyc(String userId, UpdateKycStatusRequest req) {
        String status = req.getKycStatus();
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kycStatus is required");
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_KYC.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kycStatus must be PENDING, APPROVED, or REJECTED");
        }
        Profile profile = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        profile.setKycStatus(normalized);
        profile.setUpdatedAt(Instant.now().toString());
        return repo.save(profile);
    }
}
