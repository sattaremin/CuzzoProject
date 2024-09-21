package com.amazon.dto;

import com.amazon.custom_validation.UniqueDescription;
import com.amazon.entity.Company;
import com.amazon.entity.Product;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Where(clause = "is_deleted=false")
public class
CategoryDto {


    private Long id;
    @Column(unique = true)
    @UniqueDescription(message = "Description must be unique.")
    @NotBlank(message = "Description is a required field.")
    @Size(min = 2, max = 100, message = "Description should have 2-100 characters long.")
    private String description;

    private Company company;
    private boolean hasProduct;

    private List<Product> product=new ArrayList<>();







}
