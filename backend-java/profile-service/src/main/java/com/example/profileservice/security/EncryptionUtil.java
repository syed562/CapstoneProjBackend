package com.example.profileservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Component
public class EncryptionUtil {
    private final SecretKey secretKey;
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    public EncryptionUtil(@Value("${encryption.key:mySecretEncryptionKey1234567890123456}") String encryptionKey) {
        // Ensure key is exactly 32 bytes for AES-256
        String normalizedKey = padKey(encryptionKey, 32);
        byte[] decodedKey = normalizedKey.getBytes();
        this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * Encrypt a string value using AES-256
     * Returns Base64 encoded encrypted bytes
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt a Base64-encoded encrypted value
     * Returns original plaintext
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Pad or truncate key to exact length
     */
    private String padKey(String key, int length) {
        if (key.length() == length) {
            return key;
        } else if (key.length() > length) {
            return key.substring(0, length);
        } else {
            StringBuilder padded = new StringBuilder(key);
            while (padded.length() < length) {
                padded.append("0");
            }
            return padded.toString();
        }
    }
}
