package com.challenge.JPay.repository;

import com.challenge.JPay.interfaces.CategoryTotals;
import com.challenge.JPay.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByActiveTrue();

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AccountPayable a WHERE a.category.id = :categoryId AND a.status = 'PENDING'")
    long countPendingAccountsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(a) FROM AccountPayable a WHERE a.category.id = :categoryId")
    long countAccountsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT c FROM AccountPayable a JOIN Category c ON c.id = a.category.id group by a.category.id order by COUNT(a) DESC LIMIT 1")
    Category getMostUsedCategory();

    @Query("""
        SELECT c as category,
               SUM(a.amount) as totalAmount
        FROM AccountPayable a
        JOIN a.category c
        WHERE a.category.active = true 
        AND a.transactionType = 'PASSIVO'
        GROUP BY c
    """)
    List<CategoryTotals> getCategorieTotals();
}
