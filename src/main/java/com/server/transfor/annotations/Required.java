package com.server.transfor.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RequiredValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
    String message() default "Ce champ est obligatoire";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
