package com.example.reportservice.exception;

import lombok.Data;
import java.time.Instant;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private String timestamp;

    public ErrorResponse(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now().toString();
    }
}
