package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findByCompanyId(Long companyId, Pageable pageable);
    List<Category> findByCompanyIdAndActiveTrue(Long companyId);
    Optional<Category> findByIdAndCompanyId(Long id, Long companyId);
    boolean existsBySlugAndCompanyId(String slug, Long companyId);
    long countByCompanyId(Long companyId);
}
