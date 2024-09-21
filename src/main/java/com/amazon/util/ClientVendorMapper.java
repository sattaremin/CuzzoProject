package com.amazon.util;

import com.amazon.dto.ClientVendorDto;
import com.amazon.entity.ClientVendor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ClientVendorMapper {

    private final ModelMapper modelMapper;

    private final MapperUtil mapperUtil;

    public ClientVendorMapper(ModelMapper modelMapper, MapperUtil mapperUtil) {
        this.modelMapper = modelMapper;
        this.mapperUtil = mapperUtil;
    }

    public ClientVendorDto convertToDto(ClientVendor clientVendor) {
        ClientVendorDto dto = mapperUtil.convert(clientVendor, new ClientVendorDto());
        dto.setHasInvoice(clientVendor.getInvoices() != null && !clientVendor.getInvoices().isEmpty());
        return dto;
    }

    public ClientVendor convertToEntity(ClientVendorDto clientVendorDto) {
        ClientVendor clientVendor = mapperUtil.convert(clientVendorDto, new ClientVendor());
        return clientVendor;
    }


    /*

    public ClientVendorMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ClientVendor convertToEntity(ClientVendorDto dto){
        return modelMapper.map(dto,ClientVendor.class);
    }

    public ClientVendorDto convertToDto(ClientVendor entity){
        return modelMapper.map(entity,ClientVendorDto.class);
    }



     */
}
