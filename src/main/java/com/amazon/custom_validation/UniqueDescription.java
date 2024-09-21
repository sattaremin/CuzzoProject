package com.amazon.custom_validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueDescriptionValidator.class)

public @interface UniqueDescription {
    String message() default "Description must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
