package com.amazon.dto;

import com.amazon.enums.ProductUnit;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@Where(clause = "is_deleted=false")

public class ProductDto {

    private Long id;

    @NotNull(message = "Product Name is required field.")
    @Size(min = 2, max = 100, message = "Product Name must be between 2 and 100 characters long.")
    private String name;
    private Integer quantityInStock;


    @NotNull(message = "Low Limit is required field")
    @Min(value = 1, message = "Low Limit Alert should be at least 1.")
    private Integer lowLimitAlert;

    @NotNull(message = "Product Unit is a required field.")
    private ProductUnit productUnit;

    @NotNull(message = "Category is a required field.")
    private CategoryDto category;
    private boolean hasProduct;
    private boolean hasInvoiceProduct;
    private BigDecimal unitPrice;






}
