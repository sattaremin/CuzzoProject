package com.amazon.service;


import com.amazon.dto.CategoryDto;
import com.amazon.entity.Category;
import com.amazon.exception.CategoryNotFoundException;

import java.util.List;

public interface CategoryService {


    void save(CategoryDto categoryDto);

    List<CategoryDto> listAllCategories();

    CategoryDto update(CategoryDto categoryDto);

    CategoryDto findCategoryById(Long id) throws CategoryNotFoundException;


    void deleteCategoryById(Long id) throws CategoryNotFoundException;

     List<Category> getAllCategories();
    public boolean isDescriptionUnique(String description) ;

    public List<CategoryDto> findAllByUsersCompanyIdAndIsNotDeleted();


    List<CategoryDto> getCategoriesForCurrentUser();

    List<Category> findAll();

}



