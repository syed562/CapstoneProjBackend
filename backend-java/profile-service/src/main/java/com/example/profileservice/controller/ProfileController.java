package com.example.profileservice.controller;

import com.example.profileservice.controller.dto.UpdateKycStatusRequest;
import com.example.profileservice.controller.dto.UpdateProfileRequest;
import com.example.profileservice.domain.Profile;
import com.example.profileservice.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/me")
    public Profile me(@RequestParam("userId") String userId) {
        ensureUser(userId);
        return profiles.getOwn(userId);
    }

    @PutMapping("/me")
    public Profile updateMe(@RequestParam("userId") String userId, @Valid @RequestBody UpdateProfileRequest req) {
        ensureUser(userId);
        return profiles.updateOwn(userId, req);
    }

    @GetMapping("/{userId}")
    public Profile getByUserId(@PathVariable("userId") String userId) {
        return profiles.getByUserId(userId);
    }

    @PutMapping("/{userId}/kyc")
    public Profile updateKyc(@PathVariable("userId") String userId, @Valid @RequestBody UpdateKycStatusRequest req) {
        return profiles.updateKyc(userId, req);
    }

    private void ensureUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
    }
}
