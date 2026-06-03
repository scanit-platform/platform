package com.scanit.budget.repository;

import com.scanit.budget.model.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByUserId(Long userId);
    Optional<BudgetLimit> findByUserIdAndBudgetCategoryIdAndPeriod(Long userId, Long categoryId, String period);
}
