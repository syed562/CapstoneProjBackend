package com.example.profileservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;
    private static final String TEST_SECRET_KEY = "mySecretEncryptionKey1234567890123456";

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil(TEST_SECRET_KEY);
    }

    @Test
    void testEncryptDecryptRoundTrip() {
        // Arrange
        String plainText = "sensitive-data-123";

        // Act
        String encrypted = encryptionUtil.encrypt(plainText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted); // Encrypted should differ from plaintext
        assertEquals(plainText, decrypted);
    }

    @Test
    void testEncryptProducesBase64() {
        // Arrange
        String plainText = "test-data";

        // Act
        String encrypted = encryptionUtil.encrypt(plainText);

        // Assert
        assertNotNull(encrypted);
        // Should be valid Base64
        try {
            java.util.Base64.getDecoder().decode(encrypted);
        } catch (IllegalArgumentException e) {
            fail("Encrypted data is not valid Base64");
        }
    }

    @Test
    void testEncryptEmptyString() {
        // Act
        String encrypted = encryptionUtil.encrypt("");

        // Assert
        assertEquals("", encrypted);
    }

    @Test
    void testEncryptNullString() {
        // Act
        String encrypted = encryptionUtil.encrypt(null);

        // Assert
        assertNull(encrypted);
    }

    @Test
    void testDecryptEmptyString() {
        // Act
        String decrypted = encryptionUtil.decrypt("");

        // Assert
        assertEquals("", decrypted);
    }

    @Test
    void testDecryptNullString() {
        // Act
        String decrypted = encryptionUtil.decrypt(null);

        // Assert
        assertNull(decrypted);
    }

    @Test
    void testEncryptLongString() {
        // Arrange
        String longText = "a".repeat(1000);

        // Act
        String encrypted = encryptionUtil.encrypt(longText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(longText, decrypted);
        assertNotEquals(longText, encrypted);
    }

    @Test
    void testEncryptSpecialCharacters() {
        // Arrange
        String specialText = "!@#$%^&*()|\\{}[];:'\",<>?/ 中文 العربية";

        // Act
        String encrypted = encryptionUtil.encrypt(specialText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(specialText, decrypted);
    }

    @Test
    void testEncryptNumericString() {
        // Arrange
        String numericText = "1234567890";

        // Act
        String encrypted = encryptionUtil.encrypt(numericText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(numericText, decrypted);
    }

    @Test
    void testEncryptUnicodeCharacters() {
        // Arrange
        String unicodeText = "Hello 世界 🌍 مرحبا";

        // Act
        String encrypted = encryptionUtil.encrypt(unicodeText);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(unicodeText, decrypted);
    }

    @Test
    void testDifferentPlaintextsProduceDifferentCiphertexts() {
        // Arrange
        String plainText1 = "data1";
        String plainText2 = "data2";

        // Act
        String encrypted1 = encryptionUtil.encrypt(plainText1);
        String encrypted2 = encryptionUtil.encrypt(plainText2);

        // Assert
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void testSamePlaintextConsistent() {
        // Arrange
        String plainText = "test-data";

        // Act - Encrypt same plaintext multiple times
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);

        // Assert - Both encrypt and decrypt to same value
        assertEquals(encryptionUtil.decrypt(encrypted1), encryptionUtil.decrypt(encrypted2));
    }

    @Test
    void testDecryptInvalidBase64ThrowsException() {
        // Arrange
        String invalidBase64 = "not-valid-base64!!!";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> encryptionUtil.decrypt(invalidBase64));
    }

    @Test
    void testEncryptWithDifferentKey() {
        // Arrange
        String plainText = "test-data";
        EncryptionUtil utilWithDifferentKey = new EncryptionUtil("differentSecretKey123456789012345");

        // Act
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = utilWithDifferentKey.encrypt(plainText);

        // Assert - Different keys produce different ciphertexts
        assertNotEquals(encrypted1, encrypted2);
        
        // Cross-decryption should fail or produce garbage
        assertNotEquals(plainText, utilWithDifferentKey.decrypt(encrypted1));
    }

    @Test
    void testKeyNormalization() {
        // Test with keys of different lengths
        EncryptionUtil util1 = new EncryptionUtil("shortkey");  // Will be padded
        EncryptionUtil util2 = new EncryptionUtil("a".repeat(50));  // Will be truncated

        String plainText = "test-data";

        // Act & Assert - Both should still encrypt/decrypt
        String encrypted1 = util1.encrypt(plainText);
        assertEquals(plainText, util1.decrypt(encrypted1));

        String encrypted2 = util2.encrypt(plainText);
        assertEquals(plainText, util2.decrypt(encrypted2));
    }

    @Test
    void testNewlineCharacters() {
        // Arrange
        String textWithNewlines = "line1\nline2\nline3";

        // Act
        String encrypted = encryptionUtil.encrypt(textWithNewlines);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(textWithNewlines, decrypted);
    }

    @Test
    void testTabCharacters() {
        // Arrange
        String textWithTabs = "column1\tcolumn2\tcolumn3";

        // Act
        String encrypted = encryptionUtil.encrypt(textWithTabs);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(textWithTabs, decrypted);
    }

    @Test
    void testWhitespacePreservation() {
        // Arrange
        String textWithSpaces = "  leading and trailing spaces  ";

        // Act
        String encrypted = encryptionUtil.encrypt(textWithSpaces);
        String decrypted = encryptionUtil.decrypt(encrypted);

        // Assert
        assertEquals(textWithSpaces, decrypted);
    }

    @Test
    void testEncryptionDeterminismInDecryption() {
        // While AES in ECB mode is deterministic, verify our implementation
        String plainText = "deterministic-test";
        
        // Act
        String encrypted1 = encryptionUtil.encrypt(plainText);
        String encrypted2 = encryptionUtil.encrypt(plainText);

        // Assert - Decryption should always produce same result
        assertEquals(
                encryptionUtil.decrypt(encrypted1),
                encryptionUtil.decrypt(encrypted2)
        );
    }

    @Test
    void testConstructorWithDefaultKey() {
        // Act - Constructor uses default key
        EncryptionUtil defaultKeyUtil = new EncryptionUtil("mySecretEncryptionKey1234567890123456");

        String plainText = "test";
        String encrypted = defaultKeyUtil.encrypt(plainText);
        String decrypted = defaultKeyUtil.decrypt(encrypted);

        // Assert
        assertEquals(plainText, decrypted);
    }

    @Test
    void testEmptyStringAndNullAreDifferent() {
        // Arrange
        String emptyString = "";
        String nullString = null;

        // Act
        String encryptedEmpty = encryptionUtil.encrypt(emptyString);
        String encryptedNull = encryptionUtil.encrypt(nullString);

        // Assert
        assertEquals(emptyString, encryptedEmpty);
        assertEquals(nullString, encryptedNull);
        assertNotEquals(encryptedEmpty, encryptedNull);
    }
}
