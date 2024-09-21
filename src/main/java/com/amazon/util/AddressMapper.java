package com.amazon.util;

import com.amazon.dto.AddressDto;
import com.amazon.entity.Address;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    private final ModelMapper modelMapper;

    public AddressMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Address convertToEntity(AddressDto dto) {
        return modelMapper.map(dto, Address.class);
    }

    public AddressDto convertToDto(Address entity) {
        return modelMapper.map(entity, AddressDto.class);
    }
}
