package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import com.amazon.enums.ClientVendorType;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients_vendors")
@EqualsAndHashCode
public class ClientVendor extends BaseEntity {


    private String clientVendorName;
    private String phone;
    private String website;

    @Enumerated(EnumType.STRING)
    private ClientVendorType clientVendorType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "clientVendor", cascade = CascadeType.ALL)
    private List<Invoice> invoices;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    private Boolean hasInvoice=false;




}

