package com.example.profileservice.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JPA Converter for transparent encryption/decryption of String fields.
 * Apply @Convert(converter = EncryptedStringConverter.class) to any String field to encrypt it.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    private static EncryptionUtil encryptionUtil;

    @Autowired
    public void setEncryptionUtil(EncryptionUtil encryptionUtil) {
        EncryptedStringConverter.encryptionUtil = encryptionUtil;
    }

    /**
     * Encrypt before storing in database
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionUtil == null || attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        String encrypted = encryptionUtil.encrypt(attribute);
        System.out.println("[ENCRYPTION] Plaintext: " + attribute.substring(0, Math.min(10, attribute.length())) + "... -> Encrypted: " + encrypted.substring(0, Math.min(20, encrypted.length())) + "...");
        return encrypted;
    }

    /**
     * Decrypt when reading from database
     * If decryption fails (e.g., data was not encrypted), return as-is
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionUtil == null || dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        try {
            String decrypted = encryptionUtil.decrypt(dbData);
            System.out.println("[DECRYPTION] Encrypted: " + dbData.substring(0, Math.min(20, dbData.length())) + "... -> Plaintext: " + decrypted.substring(0, Math.min(10, decrypted.length())) + "...");
            return decrypted;
        } catch (Exception e) {
            // Data might not be encrypted (legacy data), return as-is
            System.out.println("[DECRYPTION] Data not encrypted (legacy): " + dbData);
            return dbData;
        }
    }
}
