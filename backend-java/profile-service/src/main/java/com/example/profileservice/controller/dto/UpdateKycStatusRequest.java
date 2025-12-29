package com.example.profileservice.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateKycStatusRequest {
    @NotBlank
    private String kycStatus;

 
}
