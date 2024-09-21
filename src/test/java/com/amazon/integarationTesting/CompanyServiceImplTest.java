package com.amazon.integarationTesting;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.UserDto;
import com.amazon.entity.Company;
import com.amazon.enums.CompanyStatus;
import com.amazon.exception.CompanyNotFoundException;
import com.amazon.service.CompanyService;
import com.amazon.service.SecurityService;
import com.amazon.util.CompanyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class CompanyServiceImplTest {

    @Autowired
    private CompanyMapper companyMapper;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(securityContext);
    }

    private UserDto createMockUser() {
        CompanyDto mockCompanyDto = new CompanyDto();
        mockCompanyDto.setId(1L);
        UserDto mockUser = new UserDto();
        mockUser.setCompany(mockCompanyDto);
        return mockUser;
    }

    @Test
    void listAllCompanies() {
        List<CompanyDto> result = companyService.listAllCompanies();
        assertNotNull(result);
    }

    @Test
    void findAllExcludingCompanyWithIdAndSorted() {
        List<CompanyDto> result = companyService.findAllExcludingCompanyWithIdAndSorted(1L);
        assertNotNull(result);
    }

    @Test
    void findById() {
        assertThrows(CompanyNotFoundException.class, () -> companyService.findById(999L));

        CompanyDto result = companyService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void create() {
        when(securityService.getLoggedInUser()).thenReturn(createMockUser());

        CompanyDto result = companyService.getCompanyDtoByLoggedInUser();

        Company company = new Company();
        company.setId(10L);
        company.setInsertUserId(result.getId());

        CompanyDto companyDto = companyMapper.convertToDto(company);
        companyService.create(companyDto);

        assertNotNull(companyDto, "Created company should not be null");
        assertEquals(10L, companyDto.getId());
    }

    @Test
    void update() {
        when(securityService.getLoggedInUser()).thenReturn(createMockUser());

        CompanyDto mockCompanyDto = new CompanyDto();
        mockCompanyDto.setId(999L);
        mockCompanyDto.setWebsite("gozde@gmail.com");

        assertThrows(CompanyNotFoundException.class, () -> companyService.update(mockCompanyDto));

        mockCompanyDto.setId(1L);
        companyService.update(mockCompanyDto);

        CompanyDto updatedCompany = companyService.findById(1L);
        assertEquals("gozde@gmail.com", updatedCompany.getWebsite());
    }

    @Test
    void updateStatus() {
        assertThrows(CompanyNotFoundException.class, () -> companyService.updateStatus(999L, CompanyStatus.ACTIVE));

        Company result = companyService.updateStatus(1L, CompanyStatus.ACTIVE);
        assertNotNull(result);
        assertEquals(CompanyStatus.ACTIVE, result.getCompanyStatus());
    }

    @Test
    void getCompanyDtoByLoggedInUser() {
        when(securityService.getLoggedInUser()).thenReturn(createMockUser());

        CompanyDto result = companyService.getCompanyDtoByLoggedInUser();

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}