package com.scanit.budget.repository;

import com.scanit.budget.model.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    Optional<BudgetCategory> findByCategory(String category);
    boolean existsByCategory(String category);
}
