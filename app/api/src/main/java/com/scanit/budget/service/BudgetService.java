package com.scanit.budget.service;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetLimit;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    Iterable<BudgetLimit> findAll();

    BudgetLimit findById(Long id);

    List<BudgetLimit> search(Long userId, UUID generalCategoryId, UUID customCategoryId, String period);

    BudgetLimitResponseDTO saveBudgetLimit(BudgetLimitRequestDTO dto);

    BudgetLimit updateBudgetLimit(Long id, BudgetLimitRequestDTO updated);

    void deleteBudgetLimit(Long id);
}
