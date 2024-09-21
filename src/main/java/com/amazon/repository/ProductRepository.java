package com.amazon.repository;

import com.amazon.entity.Category;
import com.amazon.entity.ClientVendor;
import com.amazon.entity.Product;
import com.amazon.enums.ProductUnit;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product,Long> {


    @Query("SELECT p FROM Product p WHERE p.quantityInStock > 0")
    List<Product> findProductsInStock();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND p.productUnit = :productUnit AND p.category = :category AND p.lowLimitAlert = :lowLimitAlert")
    Product findByNameAndProductUnitAndCategoryAndLowLimitAlert(@Param("name") String name, @Param("productUnit") ProductUnit productUnit, @Param("category") Category category, @Param("lowLimitAlert") int lowLimitAlert);


    List<Product> findAllByQuantityInStockGreaterThan(int quantityInStock);

    List<Product> findAllByIsDeletedFalse();

    @Query("SELECT cv FROM ClientVendor cv " +
            "JOIN cv.company c " +
            "JOIN User u ON u.company = c " +
            "WHERE u.id = :userId")
    List<ClientVendor> findProductsByUserId(@Param("userId") Long userId);




}
