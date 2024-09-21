package com.amazon.service;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface SecurityService extends UserDetailsService {
    CompanyDto getCurrentUserCompany();

    UserDto getLoggedInUser();
}
