package com.example.authservice.security;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};:'\"<>,.?/]");

    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_PATTERN.matcher(password).find();

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    public static String getStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        StringBuilder missing = new StringBuilder();
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            missing.append("uppercase letter, ");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            missing.append("lowercase letter, ");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            missing.append("number, ");
        }
        if (!SPECIAL_PATTERN.matcher(password).find()) {
            missing.append("special character");
        }
        if (missing.length() > 0) {
            return "Password must contain: " + missing.toString().replaceAll(", $", "");
        }
        return "Password is strong";
    }
}
