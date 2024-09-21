package com.amazon.dto;
import com.amazon.enums.Months;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PaymentDto {
    @Getter
    @AllArgsConstructor
    public enum Currency{
        USD("USD"),EUR("EUR");
        private final String value;
        public String getValue() {
            return value;
        }


    }

    private Long id;
    private Integer year;
    private Months month;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private boolean isPaid;
    @Value("${stripe.api.publicKey}")
    private String companyStripeId;
    private String description;
    private CompanyDto company;

}