package com.example.loanapplication.security;

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
        return encryptionUtil.encrypt(attribute);
    }

    /**
     * Decrypt when reading from database
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionUtil == null || dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        return encryptionUtil.decrypt(dbData);
    }
}
