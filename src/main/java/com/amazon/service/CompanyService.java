package com.amazon.service;

import com.amazon.dto.CompanyDto;
import com.amazon.entity.Company;
import com.amazon.enums.CompanyStatus;

import java.util.List;

public interface CompanyService {

    List<CompanyDto> listAllCompanies();

    List<CompanyDto> findAllExcludingCompanyWithIdAndSorted(Long id);

    CompanyDto findById(Long id);

    List<CompanyDto> getCompaniesBelongsToUserCompany();

    List<CompanyDto> getCompaniesExceptCydeo();

    List<CompanyDto> getCompaniesBasedOnLoggedInUser();

    void create(CompanyDto dto);

    void update(CompanyDto dto);

    Company updateStatus(Long id, CompanyStatus status);

    CompanyDto getCompanyDtoByLoggedInUser();

}
