package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Invoice> findByIdAndCompanyId(Long id, Long companyId);
    Optional<Invoice> findBySaleId(Long saleId);
    Page<Invoice> findByCompanyIdOrderByGeneratedAtDesc(Long companyId, Pageable pageable);
    long countByCompanyId(Long companyId);
}
