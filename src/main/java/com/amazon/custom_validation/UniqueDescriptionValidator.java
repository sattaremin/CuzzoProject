package com.amazon.custom_validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.amazon.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueDescriptionValidator implements ConstraintValidator<UniqueDescription, String> {

    @Autowired
    private CategoryService categoryService; // Or another service to fetch category data


    @Override
    public void initialize(UniqueDescription constraintAnnotation) {
        // Initialization code if necessary
    }

    @Override
    public boolean isValid(String description, ConstraintValidatorContext context) {
        if (description == null || description.isEmpty()) {
            return true; // @NotBlank will handle null or empty cases
        }
        return categoryService.isDescriptionUnique(description);
    }
}