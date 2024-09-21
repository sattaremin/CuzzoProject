package com.amazon.converter;

import com.amazon.dto.CompanyDto;
import com.amazon.service.CompanyService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CompanyDTOConverter implements Converter<String, CompanyDto> {

    private final CompanyService companyService;

    public CompanyDTOConverter(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public CompanyDto convert(String source) {
        if (source == null || source.equals("")) {
            return null;
        }
        return companyService.findById(Long.parseLong(source));
    }
}
