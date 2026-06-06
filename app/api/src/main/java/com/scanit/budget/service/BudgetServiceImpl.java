package com.scanit.budget.service;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetCategory;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.repository.BudgetCategoryRepository;
import com.scanit.budget.repository.BudgetLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {
    private final BudgetLimitRepository budgetLimitRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;

    public BudgetServiceImpl(BudgetLimitRepository budgetLimitRepository,  BudgetCategoryRepository budgetCategoryRepository) {
        this.budgetLimitRepository = budgetLimitRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<BudgetLimit> findAll() {
        return budgetLimitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BudgetLimit> findById(Long id) {
        return budgetLimitRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetLimit> search(Long userId, Long categoryId, String period) {
        if (categoryId != null && period != null) {
            return budgetLimitRepository.findByUserIdAndBudgetCategoryIdAndPeriod(userId, categoryId, period);
        }

        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        if (categoryId != null) {
            return budgetLimitRepository.findByUserIdAndBudgetCategoryId(userId, categoryId);
        }

        if (period != null) {
            return budgetLimitRepository.findByUserIdAndPeriod(userId, period);
        }

        return budgetLimitRepository.findByUserId(userId);
    }

    @Override
    public Optional<BudgetCategory> findBudgetCategory(String category) {
        return budgetCategoryRepository.findByCategory(category);
    }

    @Override
    public void deleteBudgetLimit(Long id) {
        if (!budgetLimitRepository.existsById(id)) {
            throw new IllegalArgumentException("Budget limit does not exist");
        }
    }

    @Override
    public BudgetLimitResponseDTO saveBudgetLimit(BudgetLimitRequestDTO dto) {
        BudgetCategory category = budgetCategoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        BudgetLimit limit = new BudgetLimit();
        limit.setBudgetCategory(category);
        limit.setMonthlyLimit(dto.monthlyLimit());
        limit.setPeriod(dto.period());

        BudgetLimit saved = budgetLimitRepository.save(limit);

        return new BudgetLimitResponseDTO(
                saved.getId(),
                category.getCategory(),
                saved.getMonthlyLimit(),
                saved.getPeriod()
        );
    }

    @Override
    @Transactional
    public BudgetLimit updateBudgetLimit(Long id, BudgetLimitRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Update budget can't be null");
        }

        BudgetLimit existing = budgetLimitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget limit with id " + id + "does not exist"));

        if (dto.monthlyLimit() != null) {
            existing.setMonthlyLimit(dto.monthlyLimit());
        }

        if (dto.period() != null) {
            existing.setPeriod(dto.period());
        }

        return  budgetLimitRepository.save(existing);
    }
}
