package com.amazon.converter;

import com.amazon.dto.CategoryDto;
import com.amazon.exception.CategoryNotFoundException;
import com.amazon.service.CategoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryDtoConverter implements Converter<String, CategoryDto> {

    public final CategoryService categoryService;

    public CategoryDtoConverter( @Lazy CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public CategoryDto convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            return categoryService.findCategoryById(Long.parseLong(source));
        } catch (CategoryNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
