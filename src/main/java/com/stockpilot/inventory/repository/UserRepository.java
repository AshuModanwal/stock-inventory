package com.stockpilot.inventory.repository;

import com.stockpilot.inventory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByCompanyId(Long companyId, Pageable pageable);
    List<User> findByCompanyIdAndActiveTrue(Long companyId);
    long countByCompanyId(Long companyId);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<User> searchByCompany(Long companyId, String q, Pageable pageable);
}
