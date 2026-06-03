package com.scanit.budget.service;

import com.scanit.budget.model.BudgetCategory;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.repository.BudgetCategoryRepository;
import com.scanit.budget.repository.BudgetLimitRepository;
import org.springframework.stereotype.Service;

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
    public List<BudgetCategory> getBudgetCategories() {
        return budgetCategoryRepository.findAll();
    }

    @Override
    public Optional<BudgetCategory> getBudgetCategory(String category) {
        return budgetCategoryRepository.findByCategory(category);
    }

    @Override
    public List<BudgetLimit> getBudgetLimitByUser(Long userId) {
        return budgetLimitRepository.findByUserId(userId);
    }

    @Override
    public Optional<BudgetLimit> getBudgetLimit(Long userId, Long categoryId, String period) {
        return budgetLimitRepository.findByUserIdAndBudgetCategoryIdAndPeriod(userId, categoryId, period);
    }

    @Override
    public void deleteBudgetCategory(Long id) {
        if (!budgetCategoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Budget category does not exist");
        }
        budgetCategoryRepository.deleteById(id);
    }

    @Override
    public Optional<BudgetLimit> saveBudgetLimit(BudgetLimit budgetLimit) {
        return Optional.of(
                budgetLimitRepository.save(budgetLimit)
                );
    }

    @Override
    public BudgetCategory saveBudgetCategory(BudgetCategory budgetCategory) {
        if (budgetCategoryRepository.findByCategory(budgetCategory.getCategory()).isPresent()) {
            throw new IllegalArgumentException("Budget category already exists");
        }

        return budgetCategoryRepository.save(budgetCategory);
    }

    @Override
    public void updateBudgetLimit(BudgetLimit budgetLimit) {
        if (budgetLimit.getId() == null ||
                !budgetLimitRepository.existsById(budgetLimit.getId())){
            throw new IllegalArgumentException("Budget limit doesn't exist");
        }
        budgetLimitRepository.save(budgetLimit);
    }

    @Override
    public BudgetCategory updateBudgetCategory(BudgetCategory budgetCategory) {
        if (budgetCategory.getId() == null ||
                !budgetCategoryRepository.existsById(budgetCategory.getId())){
            throw new IllegalArgumentException("Budget category doesn't exist");
        }

        return budgetCategoryRepository.save(budgetCategory);
    }
}
