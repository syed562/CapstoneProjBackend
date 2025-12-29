package com.example.profileservice.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateKycStatusRequest {
    @NotBlank
    private String kycStatus;

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }
}
