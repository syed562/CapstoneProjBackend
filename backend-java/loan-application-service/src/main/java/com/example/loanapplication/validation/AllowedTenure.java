package com.example.loanapplication.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AllowedTenureValidator.class)
public @interface AllowedTenure {
    String message() default "Term must match allowed tenures";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int[] values() default {12, 24, 36};
}
