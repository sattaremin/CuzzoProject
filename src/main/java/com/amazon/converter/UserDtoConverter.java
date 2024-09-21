package com.amazon.converter;

import com.amazon.dto.UserDto;
import com.amazon.service.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter implements Converter<String, UserDto> {

    private final UserService userService;

    public UserDtoConverter(UserService userService) {
        this.userService = userService;
    }


    @Override
    public UserDto convert(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userService.findByUsername(username);
    }
}
