package com.amazon.converter;

import com.amazon.dto.RoleDto;
import com.amazon.service.RoleService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoleDtoConverter implements Converter<String, RoleDto> {

    private final RoleService roleService;

    public RoleDtoConverter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public RoleDto convert(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return roleService.findById(Long.parseLong(id));
    }
}