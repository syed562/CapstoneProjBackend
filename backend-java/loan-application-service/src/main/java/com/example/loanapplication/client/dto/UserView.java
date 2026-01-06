package com.example.loanapplication.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserView {
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;
    private String createdAt;
    private String updatedAt;
}
