package com.amazon.service.impl;

import com.amazon.dto.CompanyDto;
import com.amazon.entity.Company;
import com.amazon.entity.User;
import com.amazon.enums.CompanyStatus;
import com.amazon.exception.CompanyNotFoundException;
import com.amazon.repository.CompanyRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.CompanyService;
import com.amazon.service.SecurityService;
import com.amazon.util.CompanyMapper;
import com.amazon.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final MapperUtil mapperUtil;
    private final CompanyMapper companyMapper;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              MapperUtil mapperUtil,
                              CompanyMapper companyMapper,
                              SecurityService securityService,
                              UserRepository userRepository) {

        this.companyRepository = companyRepository;
        this.mapperUtil = mapperUtil;
        this.companyMapper = companyMapper;
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    @Override
    public List<CompanyDto> listAllCompanies() {

        List<Company> companyList = companyRepository.findAll();
        return companyList.stream().map(company -> mapperUtil.convert(company, new CompanyDto())).collect(Collectors.toList());
    }

    @Override
    public List<CompanyDto> findAllExcludingCompanyWithIdAndSorted(Long id) {
        List<Company> sortedCompanyList = companyRepository.findAllExcludingCompanyWithIdAndSorted(id);
        return sortedCompanyList.stream().map(company -> mapperUtil.convert(company, new CompanyDto())).collect(Collectors.toList());
    }

    @Override
    public CompanyDto findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
        return companyMapper.convertToDto(company);
    }

    @Override
    public List<CompanyDto> getCompaniesBelongsToUserCompany() {

        return companyRepository.findCompaniesByTitle(
                        getCompanyDtoByLoggedInUser().getTitle()).stream()
                .map(company -> mapperUtil.convert(company, new CompanyDto())).toList();
    }

    @Override
    public List<CompanyDto> getCompaniesExceptCydeo() {

        return companyRepository.findAllByTitleNotIgnoreCase("Cydeo").stream()
                .map(company -> mapperUtil.convert(company, new CompanyDto())).toList();
    }

    @Override
    public List<CompanyDto> getCompaniesBasedOnLoggedInUser() {

        if (securityService.getLoggedInUser().getRole().getDescription().equals("Root User")) {
            return getCompaniesExceptCydeo();
        } else {
            return getCompaniesBelongsToUserCompany();
        }
    }

    @Override
    @Transactional
    public void create(CompanyDto dto) {

        dto.setCompanyStatus(CompanyStatus.PASSIVE);
        Company company = companyMapper.convertToEntity(dto);
        company.setInsertDateTime(LocalDateTime.now());
        company.setInsertUserId(1L);
        company.setLastUpdateDateTime(LocalDateTime.now());
        company.setLastUpdateUserId(1L);
        companyRepository.save(company);

    }

    @Override
    public void update(CompanyDto dto) {

        Company company = companyRepository.findById(dto.getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + dto.getId() + " not found"));

        Company convertedCompany = companyMapper.convertToEntity(dto);
        convertedCompany.setId(company.getId());
        convertedCompany.setCompanyStatus(CompanyStatus.ACTIVE);

        companyRepository.save(convertedCompany);

    }

    @Override
    public Company updateStatus(Long id, CompanyStatus status) {
        // Find the company by ID
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Company with id {} not found", id);
                    return new CompanyNotFoundException("Company with id " + id + " not found");
                });

        // Update the company's status
        company.setCompanyStatus(status);
        companyRepository.save(company);

        // Log the status update
        logger.info("Updated company with id {} to status {}", id, status);

        // Fetch users associated with the company
        List<User> users = userRepository.findByCompanyId(company.getId());
        boolean enableUsers = status == CompanyStatus.ACTIVE;

        // Update and log user status
        for (User user : users) {
            user.setEnabled(enableUsers);
            userRepository.save(user);
            logger.info("User with id {} set to enabled: {}", user.getId(), enableUsers);
        }

        return company;
    }

//    @Override
//    public Company updateStatus(Long id, CompanyStatus status) {
//        Company company = companyRepository.findById(id)
//                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
//
//        company.setCompanyStatus(status);
//        companyRepository.save(company);
//
//        List<User> users = userRepository.findByCompanyId(company.getId());
//        boolean enableUsers = status == CompanyStatus.ACTIVE;
//
//        for (User user : users) {
//            user.setEnabled(enableUsers);
//            userRepository.save(user);
//        }
//        return company;
//    }

    @Override
    public CompanyDto getCompanyDtoByLoggedInUser() {
        return securityService.getLoggedInUser().getCompany();
    }
}
