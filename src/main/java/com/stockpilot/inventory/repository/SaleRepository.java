package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Page<Sale> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Sale> findByIdAndCompanyId(Long id, Long companyId);
    Page<Sale> findByCompanyIdOrderBySaleDateDesc(Long companyId, Pageable pageable);
    long countByCompanyId(Long companyId);

    @Query("SELECT s FROM Sale s WHERE s.company.id = :companyId AND s.saleDate BETWEEN :start AND :end ORDER BY s.saleDate DESC")
    List<Sale> findByCompanyAndDateRange(Long companyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.company.id = :companyId AND s.status = 'COMPLETED'")
    Double getTotalRevenue(Long companyId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.company.id = :companyId AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    Double getRevenueByDateRange(Long companyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.company.id = :companyId AND s.saleDate BETWEEN :start AND :end")
    Long countByCompanyAndDateRange(Long companyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s.soldBy.id, s.soldBy.firstName, COUNT(s), SUM(s.totalAmount) FROM Sale s WHERE s.company.id = :companyId AND s.saleDate BETWEEN :start AND :end GROUP BY s.soldBy.id, s.soldBy.firstName ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> getTopSalespeople(Long companyId, LocalDateTime start, LocalDateTime end);

    Page<Sale> findBySoldByIdAndCompanyId(Long userId, Long companyId, Pageable pageable);
}
