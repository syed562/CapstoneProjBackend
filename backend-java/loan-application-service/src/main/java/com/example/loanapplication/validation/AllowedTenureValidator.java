package com.example.loanapplication.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class AllowedTenureValidator implements ConstraintValidator<AllowedTenure, Integer> {
    private int[] allowed;

    @Override
    public void initialize(AllowedTenure constraintAnnotation) {
        this.allowed = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // handled by @NotNull where required
        }
        return Arrays.stream(allowed).anyMatch(v -> v == value);
    }
}
