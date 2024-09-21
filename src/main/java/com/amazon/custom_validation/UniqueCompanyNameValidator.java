package com.amazon.custom_validation;
import com.amazon.repository.CompanyRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueCompanyNameValidator implements ConstraintValidator<UniqueCompanyName, String>{
    private final CompanyRepository companyRepository;

    public UniqueCompanyNameValidator(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        return !companyRepository.existsByTitle(title);
    }
}
