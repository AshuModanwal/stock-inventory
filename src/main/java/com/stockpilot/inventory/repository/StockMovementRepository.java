package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByCompanyIdOrderByMovementDateDesc(Long companyId, Pageable pageable);
    Page<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId, Pageable pageable);
}
