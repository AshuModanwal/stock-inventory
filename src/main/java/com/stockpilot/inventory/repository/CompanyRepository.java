package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByGstin(String gstin);
    Page<Company> findByActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Company> search(String q, Pageable pageable);
}
