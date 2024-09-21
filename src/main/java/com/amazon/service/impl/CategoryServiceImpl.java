package com.amazon.service.impl;

import com.amazon.dto.CategoryDto;
import com.amazon.dto.UserDto;
import com.amazon.entity.Category;
import com.amazon.entity.Company;
import com.amazon.entity.User;
import com.amazon.exception.CategoryNotFoundException;
import com.amazon.exception.CompanyNotFoundException;
import com.amazon.repository.CategoryRepository;
import com.amazon.repository.CompanyRepository;
import com.amazon.service.CategoryService;
import com.amazon.service.SecurityService;
import com.amazon.service.UserService;
import com.amazon.util.MapperUtil;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MapperUtil mapperUtil;
    private final CompanyRepository companyRepository;

    private final UserService userService;

    private final SecurityService securityService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, MapperUtil mapperUtil, CompanyRepository companyRepository, UserService userService, SecurityService securityService) {
        this.categoryRepository = categoryRepository;
        this.mapperUtil = mapperUtil;
        this.companyRepository = companyRepository;
        this.userService = userService;
        this.securityService = securityService;
    }


    public void save(CategoryDto categoryDto) {
        Category category = mapperUtil.convert(categoryDto, new Category());

        UserDto currentUser = userService.getCurrentUser();
        Company company = companyRepository.findById(currentUser.getCompany().getId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        category.setCompany(company);

        categoryRepository.save(category);

    }

    @Override
    public List<CategoryDto> listAllCategories() {

            UserDto currentUser = userService.getCurrentUser();
            Long companyId = currentUser.getCompany().getId();

            return categoryRepository.findDistinctCategoriesByCompanyAndIsDeletedFalse(companyId)
                    .stream()
                    .map(category -> {
                        CategoryDto categoryDto = mapperUtil.convert(category, new CategoryDto());
                        categoryDto.setProduct(category.getProducts() != null ? category.getProducts() : new ArrayList<>());
                        categoryDto.setHasProduct(!categoryDto.getProduct().isEmpty());
                        return categoryDto;
                    })
                    .collect(Collectors.toList());
        }


    @Override
    public CategoryDto update(CategoryDto categoryDto) {

        Category category = categoryRepository.findById(categoryDto.getId()).get();

        category.setDescription(categoryDto.getDescription());
        categoryRepository.save(category);

        return mapperUtil.convert(category, categoryDto);

    }



    public CategoryDto findCategoryById(Long id) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        CategoryDto categoryDto = mapperUtil.convert(category, new CategoryDto());
        categoryDto.setProduct(category.getProducts() != null ? category.getProducts() : new ArrayList<>());
        categoryDto.setHasProduct(!categoryDto.getProduct().isEmpty());
        return categoryDto;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findByIsDeletedFalse();
    }

    public void deleteCategoryById(Long id) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }


    @Override
    public List<CategoryDto> getCategoriesForCurrentUser() {
        UserDto currentUser = userService.getCurrentUser();
        Company company = mapperUtil.convert(currentUser, new User()).getCompany();

        List<Category> categories = categoryRepository.findAllByCompanyIdAndIsDeletedFalseOrderByDescriptionAsc(company.getId());

        return categories.stream()
                .map(category -> {
                    CategoryDto categoryDto = mapperUtil.convert(category, new CategoryDto());
                    categoryDto.setProduct(category.getProducts() != null ? category.getProducts() : new ArrayList<>()); // Handle null products
                    categoryDto.setHasProduct(!categoryDto.getProduct().isEmpty());
                    return categoryDto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public boolean isDescriptionUnique(String description) {
        return !categoryRepository.existsByDescription(description);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public List<CategoryDto> findAllByUsersCompanyIdAndIsNotDeleted() {
        Long companyId = securityService.getCurrentUserCompany().getId();

        return categoryRepository.findByCompanyIdAndIsDeletedFalse(companyId).stream()
                .map(category -> {
                    CategoryDto categoryDto = mapperUtil.convert(category, new CategoryDto());
                    categoryDto.setProduct(category.getProducts() != null ? category.getProducts() : new ArrayList<>()); // Handle null products
                    categoryDto.setHasProduct(!categoryDto.getProduct().isEmpty());
                    return categoryDto;
                })
                .collect(Collectors.toList());
    }

    }







