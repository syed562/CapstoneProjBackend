package com.example.loanservice.validation;

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
            return true; // handled separately by @NotNull when required
        }
        return Arrays.stream(allowed).anyMatch(v -> v == value);
    }
}
