package com.example.loanapplication.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalRequest {
    @NotBlank(message = "Rejection remarks are required")
    private String remarks;
}
