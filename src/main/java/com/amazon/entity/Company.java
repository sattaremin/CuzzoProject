package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import com.amazon.enums.CompanyStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "companies")
@Where(clause = "is_deleted=false")
@EqualsAndHashCode(callSuper = false)
public class Company extends BaseEntity {

    private String title;
    private String phone;
    private String website;

    @Enumerated(EnumType.STRING)
    private CompanyStatus companyStatus;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
}

