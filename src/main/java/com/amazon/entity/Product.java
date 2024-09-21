package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import com.amazon.enums.ProductUnit;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@EqualsAndHashCode
public class Product extends BaseEntity {

    private String name;
    private int quantityInStock;
    private int lowLimitAlert;


    @Enumerated(EnumType.STRING)
    private ProductUnit productUnit;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}
