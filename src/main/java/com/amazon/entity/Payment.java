package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import com.amazon.enums.Months;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Payment extends BaseEntity {

    private int year;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private boolean isPaid;
    @Value("${stripe.api.publicKey}")
    private String companyStripeId;

    @Enumerated(EnumType.STRING)
    private Months month;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}