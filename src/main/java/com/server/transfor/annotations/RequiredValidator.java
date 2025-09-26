package com.server.transfor.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequiredValidator implements ConstraintValidator<Required, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Retourne false si le champ est null, vide, ou uniquement des espaces
        return value != null && !value.trim().isEmpty();
    }
}
