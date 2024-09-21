package com.amazon.custom_validation;

import com.amazon.repository.ClientVendorRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueClientVendorNameValidator implements ConstraintValidator<UniqueClientVendorName, String> {
    private final ClientVendorRepository clientVendorRepository;

    public UniqueClientVendorNameValidator(ClientVendorRepository clientVendorRepository) {
        this.clientVendorRepository = clientVendorRepository;
    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        return !clientVendorRepository.existsByClientVendorName(title);
    }
}