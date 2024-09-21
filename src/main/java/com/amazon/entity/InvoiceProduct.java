package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name= "invoice_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = false)
public class InvoiceProduct extends BaseEntity {
    private int quantity;
    private BigDecimal price;
    private int tax;
    private BigDecimal profitLoss;
    private int remainingQuantity;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "product_id" )
    private Product product;
    @Transient
    private BigDecimal totalWithTax;






}


