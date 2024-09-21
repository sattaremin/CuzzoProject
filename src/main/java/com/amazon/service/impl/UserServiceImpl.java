package com.amazon.service.impl;

import com.amazon.dto.UserDto;
import com.amazon.entity.User;
import com.amazon.exception.UserNotFoundException;
import com.amazon.repository.UserRepository;
import com.amazon.service.SecurityService;
import com.amazon.service.UserService;
import com.amazon.util.MapperUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           MapperUtil mapperUtil,
                           @Lazy SecurityService securityService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapperUtil = mapperUtil;
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto findByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username);
            return mapperUtil.convert(user, new UserDto());
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException("User : " + username + " could not found");
        }
    }

    @Override
    public List<UserDto> listAllByLoggedInUser() {
        User currentUser = mapperUtil.convert(securityService.getLoggedInUser(), new User());
        List<User> userList;
        if (currentUser.getRole().getDescription().equals("Root User")) {
            userList = userRepository.findAllByRole_Description("Admin");
        } else {
            userList = userRepository.findByCompanyId(currentUser.getCompany().getId());
        }
        return userList.stream()
                .sorted(Comparator.comparing((User u) -> u.getCompany().getTitle())
                        .thenComparing(u -> u.getRole().getDescription()))
                .map(entity -> {
                    UserDto dto = mapperUtil.convert(entity, new UserDto());
                    dto.setOnlyAdmin(dto.getRole().getDescription().equals("Admin") && this.checkIfOnlyAdmin(dto));
                    return dto;
                })
                .toList();
    }

    @Override
    public UserDto findById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        UserDto dto = mapperUtil.convert(user, new UserDto());
        dto.setOnlyAdmin(dto.getRole().getDescription().equals("Admin") && this.checkIfOnlyAdmin(dto));
        return dto;
    }

    @Override
    public void save(UserDto user) {

        User userEntity = mapperUtil.convert(user, new User());
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
    }

    @Override
    public UserDto update(UserDto user) {

        save(user);

        return findById(user.getId());
    }


    private boolean checkIfOnlyAdmin(UserDto userDto) {

        if (userDto != null && Boolean.TRUE.equals(userDto.getIsOnlyAdmin())) {
            return true;
        }
        return false;
    }


    @Override
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        return mapperUtil.convert(user, new UserDto());

    }


    public void deleteById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsDeleted(true);
            userRepository.save(user);
        }
    }

    @Override
    public boolean isUserNameExists(UserDto userDto) {
        User user = userRepository.findByUsername(userDto.getUsername());
        if (user == null) return false;
        return !Objects.equals(userDto.getId(), user.getId());
    }


}


