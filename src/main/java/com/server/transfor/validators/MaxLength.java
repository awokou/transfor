package com.server.transfor.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxLengthValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxLength {

    String message() default "La longueur dépasse la limite autorisée";

    int value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
