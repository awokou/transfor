package com.server.transfor.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxLengthValidator implements ConstraintValidator<MaxLength, String> {

    private int max;

    @Override
    public void initialize(MaxLength constraintAnnotation) {
        this.max = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Le champ doit être valide si non null et <= max
        // Sinon, c’est @NotBlank qui gère null / vide
        if (value == null) return true;
        return value.length() <= max;
    }
}
