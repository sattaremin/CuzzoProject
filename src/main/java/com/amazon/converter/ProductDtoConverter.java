package com.amazon.converter;

import com.amazon.dto.ProductDto;
import com.amazon.service.ProductService;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding

public class ProductDtoConverter implements Converter<String, ProductDto> {

    private final ProductService productService;

    public ProductDtoConverter( @Lazy ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ProductDto convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        return productService.getProductById(Long.valueOf(source));
    }
}
