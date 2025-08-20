package com.challenge.JPay.repository;

import com.challenge.JPay.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);

    Page<Category> findByActiveTrue(Pageable pageable);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.category.id = :categoryId AND a.status = 'PENDING'")
    long countPendingAccountsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.category.id = :categoryId")
    long countAccountsByCategory(@Param("categoryId") Long categoryId);
}
