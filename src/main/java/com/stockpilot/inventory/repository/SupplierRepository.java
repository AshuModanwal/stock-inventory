package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);
    List<Supplier> findByCompanyIdAndActiveTrue(Long companyId);
    long countByCompanyId(Long companyId);
}
