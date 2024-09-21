package com.amazon.repository;

import com.amazon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByCompanyId(Long companyId);

    User findByUsername(String username);

    List<User> findAllByRole_Description(String roleDescription);

    Integer countAllByCompany_IdAndRole_Description(Long companyId, String roleDescription);

    List<User> findAllByCompanyIdAndRole_Description(Long companyId, String roleDescription);




}
