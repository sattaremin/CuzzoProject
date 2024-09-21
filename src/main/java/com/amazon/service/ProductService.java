package com.amazon.service;

import com.amazon.dto.ClientVendorDto;
import com.amazon.dto.ProductDto;
import com.amazon.entity.Category;
import com.amazon.entity.Product;
import com.amazon.enums.ProductUnit;

import java.util.List;

public interface ProductService {
    List<ProductDto> listAllProductsByCategoryAndProductName();

    ProductDto getProductById(Long id);

    List<ProductDto> findAvailableProductsForInvoice(Long invoiceId);

    void create(ProductDto product);

    Product findByName(String name, ProductUnit productUnit, Category category, int lowLimitAlert);

    void delete(Long id);




    void edit(ProductDto product);
    void updateOrCreate(ProductDto productDto);

    boolean hasInvoice(Long productId);

    List<ClientVendorDto> findAllVendors();
    List<ProductDto> findAllInStockProducts();

    List<ProductDto>listProductsInStock();

    Boolean validStock(Long productId, Integer quantity);



}