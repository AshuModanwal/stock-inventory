package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Product> findByIdAndCompanyId(Long id, Long companyId);
    boolean existsBySkuAndCompanyId(String sku, Long companyId);
    List<Product> findByCompanyIdAndActiveTrue(Long companyId);
    long countByCompanyId(Long companyId);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND p.quantity <= p.lowStockThreshold AND p.active = true")
    List<Product> findLowStockByCompany(Long companyId);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND p.quantity = 0 AND p.active = true")
    List<Product> findOutOfStockByCompany(Long companyId);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Product> searchByCompany(Long companyId, String q, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.company.id = :companyId AND p.category.id = :categoryId AND p.active = true")
    Page<Product> findByCompanyAndCategory(Long companyId, Long categoryId, Pageable pageable);

    @Query("SELECT SUM(p.quantity * p.costPrice) FROM Product p WHERE p.company.id = :companyId AND p.active = true")
    Double getTotalInventoryValue(Long companyId);

    @Query("SELECT SUM(p.quantity) FROM Product p WHERE p.company.id = :companyId AND p.active = true")
    Long getTotalStockCount(Long companyId);
}
