package com.amazon.service;

import com.amazon.dto.RoleDto;
import java.util.List;

public interface RoleService {

    RoleDto findById(Long id);

    List<RoleDto> listAllRoles();

}
