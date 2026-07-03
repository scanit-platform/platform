package com.scanit.budget.repository;

import com.scanit.budget.model.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByUserId(Long userId);

    List<BudgetLimit> findByUserIdAndPeriod(Long userId, String period);

    List<BudgetLimit> findByUserIdAndCategoryId(Long userId, UUID categoryId);

    List<BudgetLimit> findByUserIdAndCategoryIdAndPeriod(Long userId, UUID categoryId, String period);
}
