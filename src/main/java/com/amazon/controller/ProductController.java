package com.amazon.controller;

import com.amazon.dto.ProductDto;
import com.amazon.entity.Product;
import com.amazon.enums.ProductUnit;
import com.amazon.service.CategoryService;
import com.amazon.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public String listAllProducts(Model model) {
        model.addAttribute("products", productService.listAllProductsByCategoryAndProductName());
        return "/product/product-list";
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long productId,RedirectAttributes redirectAttributes) {

        try {
            productService.delete(productId);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully");
        }catch (IllegalStateException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/list";

    }

    @GetMapping("/create")
    public String createProduct(Model model) {
        model.addAttribute("newProduct", new Product());
        model.addAttribute("productUnits", Arrays.stream(ProductUnit.values()).toList());
        model.addAttribute("categories", categoryService.findAllByUsersCompanyIdAndIsNotDeleted());
        return "/product/product-create";
    }

    @PostMapping("/create")
    public String insertProduct(@ModelAttribute("newProduct") @Valid ProductDto productDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productUnits", Arrays.stream(ProductUnit.values()).toList());
            model.addAttribute("categories", categoryService.findAllByUsersCompanyIdAndIsNotDeleted());
            return "/product/product-create";
        }
        productService.updateOrCreate(productDto);
        return "redirect:/products/list";
    }

    @GetMapping("/update/{id}")
    public String editCompany(@PathVariable("id") Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        model.addAttribute("categories", categoryService.findAllByUsersCompanyIdAndIsNotDeleted());
        model.addAttribute("productUnits", Arrays.stream(ProductUnit.values()).toList());
        return "/product/product-update";
    }

    @PostMapping("/update/{id}")
    public String updateCompany(@Valid @ModelAttribute("product") ProductDto productDto,
                                BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "/product/product-update";
        }
        productService.updateOrCreate(productDto);
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");
        return "redirect:/products/list";
    }
}