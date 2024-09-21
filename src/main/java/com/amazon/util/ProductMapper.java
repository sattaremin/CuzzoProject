package com.amazon.util;

import com.amazon.dto.ProductDto;
import com.amazon.entity.Product;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    private final ModelMapper modelMapper;

    public ProductMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Product convertToEntity(ProductDto dto) {
        return modelMapper.map(dto, Product.class);
    }

    public ProductDto convertToDto(Product entity) {
        return modelMapper.map(entity, ProductDto.class);
    }
}
