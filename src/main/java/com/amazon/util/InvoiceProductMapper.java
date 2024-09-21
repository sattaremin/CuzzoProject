package com.amazon.util;

import com.amazon.dto.InvoiceProductDto;
import com.amazon.entity.InvoiceProduct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class InvoiceProductMapper {

    private final ModelMapper modelMapper;


    public InvoiceProductMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public InvoiceProduct convertToEntity(InvoiceProductDto invoiceProductDto){
        return modelMapper.map(invoiceProductDto,InvoiceProduct.class);
    }

    public InvoiceProductDto convertToDto(InvoiceProduct invoiceProduct){
        return modelMapper.map(invoiceProduct,InvoiceProductDto.class);
    }
}
