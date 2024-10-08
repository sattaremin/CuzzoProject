package com.amazon.custom_validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueClientVendorNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueClientVendorName {

    String message() default "Client Vendor name must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
