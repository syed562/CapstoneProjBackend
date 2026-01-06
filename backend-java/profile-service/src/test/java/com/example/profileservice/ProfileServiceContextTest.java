package com.example.profileservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.profileservice.service.ProfileService;

@SpringBootTest
class ProfileServiceContextTest {

    @Autowired
    private ProfileService profileService;

    @Test
    void contextLoads() {
        assertNotNull(profileService, "ProfileService should be autowired");
    }
}
