package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Page<PurchaseOrder> findByCompanyId(Long companyId, Pageable pageable);
    Optional<PurchaseOrder> findByIdAndCompanyId(Long id, Long companyId);
    long countByCompanyId(Long companyId);
}
