package com.scanit.budget.service;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetCategory;
import com.scanit.budget.model.BudgetLimit;

import java.util.List;
import java.util.Optional;

public interface BudgetService {
    Iterable<BudgetLimit>findAll();
    BudgetLimit findById(Long id);
    List<BudgetLimit> search(Long userId, Long categoryId, String period);
    Optional<BudgetCategory> findBudgetCategory(String category);
    BudgetLimitResponseDTO saveBudgetLimit(BudgetLimitRequestDTO dto);
    BudgetLimit updateBudgetLimit(Long id,  BudgetLimitRequestDTO updated);
    void deleteBudgetLimit(Long id);
}
