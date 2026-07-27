package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);
    List<Customer> findByCompanyIdAndActiveTrue(Long companyId);
    long countByCompanyId(Long companyId);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) OR c.phone LIKE CONCAT('%',:q,'%'))")
    Page<Customer> searchByCompany(Long companyId, String q, Pageable pageable);
}
