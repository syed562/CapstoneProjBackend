package com.example.authservice.controller.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String userId;
    private String username;
    private String role;
    private String message;

    public LoginResponse(String token, String userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = "Login successful";
    }
}
