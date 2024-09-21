package com.amazon.util;

import com.amazon.dto.InvoiceDto;
import com.amazon.entity.Invoice;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class InvoiceMapper {

    private final ModelMapper modelMapper;

    public InvoiceMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Invoice convertToEntity(InvoiceDto dto){
        return modelMapper.map(dto,Invoice.class);
    }

    public InvoiceDto convertToDto(List<Invoice> entity){
        return modelMapper.map(entity,InvoiceDto.class);
    }

    public InvoiceDto convertToDto(Invoice entity){
        return modelMapper.map(entity,InvoiceDto.class);
    }
}
