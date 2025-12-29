package com.example.loanservice.controller.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
@Data
public class UpdateStatusRequest {
    @NotBlank
    private String status;

}
