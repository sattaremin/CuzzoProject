package com.amazon.service;

import com.amazon.dto.UserDto;
import com.amazon.exception.UserNotFoundException;

import java.util.List;


public interface UserService {

    UserDto findByUsername(String username) throws UserNotFoundException;

    List<UserDto> listAllByLoggedInUser() throws UserNotFoundException;

    UserDto findById(Long id) throws UserNotFoundException;

    void save(UserDto user) throws UserNotFoundException;

    UserDto update(UserDto user) throws UserNotFoundException;

    UserDto getCurrentUser() throws UserNotFoundException;

    boolean isUserNameExists(UserDto userDto) throws UserNotFoundException;
}
