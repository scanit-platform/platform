package com.scanit.budget.service;

import com.scanit.budget.model.BudgetCategory;
import com.scanit.budget.model.BudgetLimit;

import java.util.List;
import java.util.Optional;

public interface BudgetService {
    List<BudgetCategory> getBudgetCategories();
    Optional<BudgetCategory> getBudgetCategory(String category);
    List<BudgetLimit> getBudgetLimitByUser(Long userId);
    Optional<BudgetLimit> getBudgetLimit(Long userId, Long categoryId, String period);
    Optional<BudgetLimit> saveBudgetLimit(BudgetLimit budgetLimit);
    void updateBudgetLimit(BudgetLimit budgetLimit);
    void deleteBudgetCategory(Long id);
    BudgetCategory saveBudgetCategory(BudgetCategory budgetCategory);
    BudgetCategory updateBudgetCategory(BudgetCategory budgetCategory);
}
