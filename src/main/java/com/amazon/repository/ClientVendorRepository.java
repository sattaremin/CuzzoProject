package com.amazon.repository;

import com.amazon.entity.ClientVendor;
import com.amazon.enums.ClientVendorType;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ClientVendorRepository extends JpaRepository<ClientVendor, Long> {

    ClientVendor findByClientVendorType(ClientVendorType vendorType);

    @Query("SELECT cv FROM ClientVendor cv " +
            "JOIN cv.company c " +
            "JOIN User u ON u.company = c " +
            "WHERE u.id = :userId")
    List<ClientVendor> findClientVendorsByUserId(@Param("userId") Long userId);

    List<ClientVendor> findAllByCompanyTitle(String companyTitle);

    List<ClientVendor> findAllByCompanyTitleOrderByClientVendorName(String clientVendor);

    boolean existsByClientVendorName(String name);
}
