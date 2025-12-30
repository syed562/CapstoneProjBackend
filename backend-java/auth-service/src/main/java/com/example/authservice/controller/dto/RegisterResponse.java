package com.example.authservice.controller.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private String userId;
    private String username;
    private String email;
    private String role;
    private String message;

    public RegisterResponse(String userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = "User registered successfully";
    }
}
