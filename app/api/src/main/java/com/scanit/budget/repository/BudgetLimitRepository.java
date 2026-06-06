package com.scanit.budget.repository;

import com.scanit.budget.model.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByUserId(Long userId);
    List<BudgetLimit> findByUserIdAndPeriod(Long userId, String period);
    List<BudgetLimit> findByUserIdAndBudgetCategoryId(Long userId, Long categoryId);
    List<BudgetLimit> findByUserIdAndBudgetCategoryIdAndPeriod(Long userId, Long categoryId, String period);
}
