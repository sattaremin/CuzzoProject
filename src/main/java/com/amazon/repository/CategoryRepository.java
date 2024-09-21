package com.amazon.repository;

import com.amazon.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category,Long> {

    List<Category> findByIsDeletedFalse();
    List<Category> findAllByCompanyIdAndIsDeletedFalseOrderByDescriptionAsc(@Param("companyId") Long companyId);
    boolean existsByDescription(String description);
    @Query("SELECT DISTINCT c FROM Category c WHERE c.company.id = :companyId AND c.isDeleted = false ORDER BY c.description ASC")
    List<Category> findDistinctCategoriesByCompanyAndIsDeletedFalse(@Param("companyId") Long companyId);

    List<Category> findByCompanyIdAndIsDeletedFalse(Long companyId);




}
