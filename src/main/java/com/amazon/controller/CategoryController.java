package com.amazon.controller;

import com.amazon.dto.CategoryDto;
import com.amazon.entity.Category;
import com.amazon.exception.CategoryNotFoundException;
import com.amazon.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public String listAllCategories(Model model) {
        model.addAttribute("categories", categoryService.listAllCategories());
        return "/category/category-list";
    }

    @GetMapping("/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("newCategory", new Category());
        return "/category/category-create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("newCategory") @Valid CategoryDto categoryDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "/category/category-create";
        }
        categoryService.save(categoryDto);
        model.addAttribute("categories", categoryService.listAllCategories());
        return "redirect:/categories/list";
    }

    @GetMapping("/update/{id}")
    public String updateCategoryForm1(@PathVariable("id") Long id, Model model) throws CategoryNotFoundException {
        model.addAttribute("category", categoryService.findCategoryById(id));
        return "category/category-update";
    }

    @PostMapping("/update/{id}")
    public String updateCategoryForm2(@ModelAttribute("category") @Valid CategoryDto categoryDto, BindingResult result, Model model) throws CategoryNotFoundException {
        if (result.hasErrors()) {
            return "category/category-update";
        }
        categoryService.update(categoryDto);
        return "redirect:/categories/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) throws CategoryNotFoundException {
        categoryService.deleteCategoryById(id);
        return "redirect:/categories/list";
    }

}
