package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import lombok.*;
import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = false)    //to use our classes in DS, we have to override this method
@Table(name = "categories")
public class Category extends BaseEntity {

    private String description;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;


}