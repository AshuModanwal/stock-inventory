package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);

    @Query("SELECT si.product.id, si.productName, SUM(si.quantity), SUM(si.totalPrice) FROM SaleItem si " +
           "JOIN si.sale s WHERE s.company.id = :companyId AND s.saleDate BETWEEN :start AND :end AND s.status = 'COMPLETED' " +
           "GROUP BY si.product.id, si.productName ORDER BY SUM(si.quantity) DESC")
    List<Object[]> getTopProducts(Long companyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT si.product.category.name, SUM(si.totalPrice) FROM SaleItem si " +
           "JOIN si.sale s WHERE s.company.id = :companyId AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end " +
           "GROUP BY si.product.category.name ORDER BY SUM(si.totalPrice) DESC")
    List<Object[]> getRevenueByCategory(Long companyId, LocalDateTime start, LocalDateTime end);
}
