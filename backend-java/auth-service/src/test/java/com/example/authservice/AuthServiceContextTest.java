package com.example.authservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.authservice.service.AuthService;

@SpringBootTest
class AuthServiceContextTest {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
        assertNotNull(authService, "AuthService should be autowired");
    }
}
