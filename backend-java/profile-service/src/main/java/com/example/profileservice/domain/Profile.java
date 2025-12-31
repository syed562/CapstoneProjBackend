package com.example.profileservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import lombok.Data;
import com.example.profileservice.security.EncryptedStringConverter;

@Entity
@Table(name = "profiles")
@Data
public class Profile {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    @Convert(converter = EncryptedStringConverter.class)
    private String email;
    @Convert(converter = EncryptedStringConverter.class)
    private String phone;
    @Convert(converter = EncryptedStringConverter.class)
    private String addressLine1;
    @Convert(converter = EncryptedStringConverter.class)
    private String addressLine2;
    @Convert(converter = EncryptedStringConverter.class)
    private String city;
    @Convert(converter = EncryptedStringConverter.class)
    private String state;
    @Convert(converter = EncryptedStringConverter.class)
    private String postalCode;
    @Convert(converter = EncryptedStringConverter.class)
    private String country;
    private String kycStatus;
    @Convert(converter = EncryptedStringConverter.class)
    private String creditScore;     // Encrypted: Credit score (0-900)
    @Convert(converter = EncryptedStringConverter.class)
    private String annualIncome;    // Encrypted: Annual income for eligibility
    @Convert(converter = EncryptedStringConverter.class)
    private String totalLiabilities; // Encrypted: Total outstanding liabilities
    private String createdAt;
    private String updatedAt;

 
}
