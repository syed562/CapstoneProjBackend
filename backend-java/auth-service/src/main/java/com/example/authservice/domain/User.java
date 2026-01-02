package com.example.authservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
public class User {
    // Status Constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String status = STATUS_ACTIVE; 

    @Column(nullable = false)
    private String createdAt;

    @Column(nullable = false)
    private String updatedAt;

    // Helper Methods
    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    public void deactivate() {
        this.status = STATUS_INACTIVE;
    }

    public void activate() {
        this.status = STATUS_ACTIVE;
    }

    public void suspend() {
        this.status = STATUS_SUSPENDED;
    }
}
