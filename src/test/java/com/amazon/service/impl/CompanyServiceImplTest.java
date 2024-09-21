package com.amazon.service.impl;

import com.amazon.dto.AddressDto;
import com.amazon.dto.CompanyDto;
import com.amazon.dto.UserDto;
import com.amazon.entity.Address;
import com.amazon.entity.Company;
import com.amazon.entity.User;
import com.amazon.enums.CompanyStatus;
import com.amazon.exception.CompanyNotFoundException;
import com.amazon.repository.CompanyRepository;
import com.amazon.repository.UserRepository;
import com.amazon.service.SecurityService;
import com.amazon.util.CompanyMapper;
import com.amazon.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private MapperUtil mapperUtil;
    @Mock
    private CompanyMapper companyMapper;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    Company company1;
    CompanyDto companydto1;
    Address address1;
    Company company2;
    CompanyDto companyDto2;
    AddressDto addressDto1 = new AddressDto();
    AddressDto addressDto2 = new AddressDto();
    User user1;
    User user2;
    User loggedInUser;


    @BeforeEach
    void setUp() {
        company1 = new Company();
        company1.setId(1L);
        company1.setTitle("Title1");
        company1.setAddress(address1);
        company1.setWebsite("https://www.gozde.com");
        company1.setIsDeleted(false);
        company1.setCompanyStatus(CompanyStatus.ACTIVE);
        company1.setPhone("1234567890");


        company2 = new Company();
        company2.setId(2L);
        company2.setTitle("Title2");
        company2.setAddress(address1);
        company2.setWebsite("https://www.cydeo.com");
        company2.setIsDeleted(false);
        company2.setCompanyStatus(CompanyStatus.ACTIVE);
        company2.setPhone("1234567890");

        companydto1 = new CompanyDto();
        companydto1.setId(1L);
        companydto1.setTitle("Title1");
        companydto1.setAddress(addressDto1);
        companydto1.setWebsite("https://www.gozde.com");
        companydto1.setCompanyStatus(CompanyStatus.ACTIVE);
        companydto1.setPhone("1234567890");

        companyDto2 = new CompanyDto();
        companyDto2.setId(2L);
        companyDto2.setTitle("Title2");
        companyDto2.setAddress(addressDto2);
        companyDto2.setWebsite("https://www.cydeo.com");
        companyDto2.setCompanyStatus(CompanyStatus.ACTIVE);
        companyDto2.setPhone("1234567890");

        user1 = new User();
        user1.setId(1L);
        user1.setCompany(company1);
        user1.setEnabled(false);

        user2 = new User();
        user2.setId(2L);
        user2.setCompany(company1);
        user2.setEnabled(false);

       loggedInUser = new User();
       loggedInUser.setId(1L);
       loggedInUser.setCompany(company1);
    }

    private List<Company> getCompanyList() {
        List<Company> companyList = new ArrayList<>();
        companyList.add(company1);
        companyList.add(company2);
        return companyList;
    }

    private List<CompanyDto> getCompanyDtoList() {

        List<CompanyDto> companyDtoList = new ArrayList<>();
        companyDtoList.add(companydto1);
        companyDtoList.add(companyDto2);
        return companyDtoList;

    }

    @Test
    public void should_List_All_Company() {
        //Given

        when(companyRepository.findAll()).thenReturn(getCompanyList());

        when(mapperUtil.convert(company1, new CompanyDto())).thenReturn(companydto1);
        when(mapperUtil.convert(company2, new CompanyDto())).thenReturn(companyDto2);
        //When

        List<CompanyDto> expectedList = getCompanyDtoList();
        List<CompanyDto> actualList = companyService.listAllCompanies();

        //then

        assertEquals(expectedList, actualList);

    }

    private List<Company> filteredCompanyList() {
        List<Company> companyList = new ArrayList<>();
        companyList.add(company2);
        return companyList;
    }


    @Test
    public void should_Find_all_excluding_Company_with_Id_and_sorted() {

        //given
        Long excludedId = 1L;
        when(companyRepository.findAllExcludingCompanyWithIdAndSorted(excludedId)).thenReturn(filteredCompanyList());
        when(mapperUtil.convert(company2, new CompanyDto())).thenReturn(companyDto2);

        //when

        List<CompanyDto> actualList = companyService.findAllExcludingCompanyWithIdAndSorted(excludedId);
        List<CompanyDto> expectedList = new ArrayList<>();
        expectedList.add(companyDto2);
        //then
        assertEquals(expectedList, actualList);
    }

    private Company findCompanyById() {
        Company company = new Company();
        company.setTitle("Title1");
        company.setWebsite("https://www.gozde.com");
        company.setIsDeleted(false);
        company.setCompanyStatus(CompanyStatus.ACTIVE);
        return company;
    }

    @Test
    public void should_Find_By_Id() {

        //given
        Long id = 1L;

        Company company = findCompanyById();

        when(companyRepository.findById(id)).thenReturn(Optional.of(company));
        when(companyMapper.convertToDto(company)).thenReturn(companydto1);
        //when

        CompanyDto actualCompany = companyService.findById(id);
        CompanyDto expectedCompany = companydto1;
        //then

        assertEquals(expectedCompany, actualCompany);
    }

    @Test
    void should_Throw_CompanyNotFoundException_When_Company_Not_Found() {
        // Given
        Long companyId = 1L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // When
        Executable executable = () -> companyService.findById(companyId);

        // Then
        assertThrows(CompanyNotFoundException.class, executable);
    }

    @Test
    @Transactional
    void should_Create_Company_With_Passive_status() {

        //given

        when(companyMapper.convertToEntity(companydto1)).thenReturn(company1);

        //when

        companyService.create(companydto1);

        //then

        verify(companyMapper, times(1)).convertToEntity(companydto1);
        verify(companyRepository, times(1)).save(company1);
        assertEquals(CompanyStatus.PASSIVE, companydto1.getCompanyStatus());


    }

    @Test
    void should_update_company() {
        Long companyId = 1L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company1));
        when(companyMapper.convertToEntity(companydto1)).thenReturn(company1);

        companyService.update(companydto1);

        verify(companyMapper, times(1)).convertToEntity(companydto1);
        verify(companyRepository, times(1)).save(company1);
        assertEquals(companyId, company1.getId());
        assertEquals(CompanyStatus.ACTIVE, companydto1.getCompanyStatus());


    }

    @Test
    void should_update_company_status() {
        Long companyId = 1L;
        CompanyStatus newStatus = CompanyStatus.ACTIVE;
        List<User> users = Arrays.asList(user1, user2);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company1));
        when(userRepository.findByCompanyId(companyId)).thenReturn(users);

        // When
        Company updatedCompany = companyService.updateStatus(companyId, newStatus);

        // Then
        verify(companyRepository, times(1)).findById(companyId);
        verify(companyRepository, times(1)).save(company1);
        verify(userRepository, times(1)).findByCompanyId(companyId);
        verify(userRepository, times(2)).save(any(User.class));

        assertEquals(newStatus, updatedCompany.getCompanyStatus());
        assertEquals(true, user1.isEnabled());
        assertEquals(true, user2.isEnabled());


    }

    private UserDto loggedInUser() {
       UserDto userDto = new UserDto();
       userDto.setCompany(companydto1);
       return userDto;
    }


    @Test
    void should_return_companyDto_of_logged_in_user() {
        // Given
        when(securityService.getLoggedInUser()).thenReturn(loggedInUser());

        // When
        CompanyDto actualCompanyDto = companyService.getCompanyDtoByLoggedInUser();

        // Then
        assertEquals(companydto1, actualCompanyDto);
    }


}