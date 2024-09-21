package com.amazon.service.impl;

import com.amazon.dto.CompanyDto;
import com.amazon.dto.UserDto;
import com.amazon.entity.User;
import com.amazon.entity.common.UserPrincipal;
import com.amazon.repository.UserRepository;
import com.amazon.service.SecurityService;
import com.amazon.service.UserService;
import com.amazon.util.MapperUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final MapperUtil mapperUtil;


    public SecurityServiceImpl(UserRepository userRepository, UserService userService, MapperUtil mapperUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public CompanyDto getCurrentUserCompany() {
        if (getLoggedInUser() != null && getLoggedInUser().getCompany() != null) {
            return mapperUtil.convert(getLoggedInUser().getCompany(), new CompanyDto());
        }
            return null;

    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new UserPrincipal(user);
    }

    @Override
    public UserDto getLoggedInUser() {
        var currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(currentUsername);
    }
}
