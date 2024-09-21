package com.amazon.entity;

import com.amazon.entity.common.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
@Where(clause = "is_deleted=false")
@EqualsAndHashCode
public class User extends BaseEntity {

    @Column(unique = true)
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private boolean enabled;

    @ManyToOne
    private Role role;

    @ManyToOne
    private Company company;

}

