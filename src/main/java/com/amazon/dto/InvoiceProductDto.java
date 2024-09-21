package com.amazon.dto;

import lombok.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class InvoiceProductDto {
    private Long id;

    @NotNull(message = "Quantity is a required field.")
    @Min(value = 1, message = "Quantity cannot be less than 1")
    @Max(value = 100, message = "Quantity cannot be greater than 100")
    private Integer quantity;

    @NotNull(message = "Price is a required field.")
    @DecimalMin(value = "1.0", message = "Price should be at least $1")
    private BigDecimal price;

    @NotNull(message = "Tax is a required field.")
    @Min(value = 0, message = "Tax should be at least 0%.")
    @Max(value = 20, message = "Tax should be no more than 20%.")
    private Integer tax;

    private BigDecimal total;
    private BigDecimal profitLoss;
    private Integer remainingQuantity;
    private InvoiceDto invoice;

    @NotNull(message = "Product is a required field.")
    private ProductDto product;
    private double unitPrice;

    public BigDecimal getTotal() {
        return this.price.multiply(BigDecimal.valueOf(this.quantity))
                .add(this.price.multiply(BigDecimal.valueOf(this.quantity)).multiply(BigDecimal.valueOf(this.tax)).divide(BigDecimal.valueOf(100)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceProductDto that = (InvoiceProductDto) o;
        return Objects.equals(quantity, that.quantity) &&
                Objects.equals(price, that.price) &&
                Objects.equals(tax, that.tax);

    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, price ,tax);
    }


}