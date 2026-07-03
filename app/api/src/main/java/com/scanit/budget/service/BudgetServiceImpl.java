package com.scanit.budget.service;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.exception.BudgetNotFoundException;
import com.scanit.budget.mapper.BudgetMapper;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.repository.BudgetLimitRepository;
import com.scanit.category.model.Category;
import com.scanit.category.repository.CategoryRepository;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {
    private final BudgetLimitRepository budgetLimitRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    @Override
    public Iterable<BudgetLimit> findAll() {
        return budgetLimitRepository.findAll();
    }

    @Override
    public BudgetLimit findById(Long id) {
        return budgetLimitRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
    }

    @Override
    public List<BudgetLimit> search(Long userId, UUID categoryId, String period) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        if (categoryId != null && period != null) {
            return budgetLimitRepository.findByUserIdAndCategoryIdAndPeriod(userId, categoryId, period);
        }

        if (categoryId != null) {
            return budgetLimitRepository.findByUserIdAndCategoryId(userId, categoryId);
        }

        if (period != null) {
            return budgetLimitRepository.findByUserIdAndPeriod(userId, period);
        }

        return budgetLimitRepository.findByUserId(userId);
    }

    @Override
    public Optional<Category> findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    @Transactional
    public BudgetLimitResponseDTO saveBudgetLimit(BudgetLimitRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Budget limit request is required");
        }

        User user = findUser(dto.userId());
        Category category = findActiveAvailableCategory(dto.categoryId(), dto.userId());

        BudgetLimit limit = new BudgetLimit();
        limit.setUser(user);
        limit.setCategory(category);
        limit.setMonthlyLimit(dto.monthlyLimit());
        limit.setPeriod(dto.period());

        return budgetMapper.toBudgetLimitResponse(budgetLimitRepository.save(limit));
    }

    @Override
    @Transactional
    public BudgetLimit updateBudgetLimit(Long id, BudgetLimitRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Update budget can't be null");
        }

        BudgetLimit existing = budgetLimitRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));

        if (dto.categoryId() != null) {
            Long ownerId = existing.getUser().getId();
            existing.setCategory(findActiveAvailableCategory(dto.categoryId(), ownerId));
        }

        if (dto.monthlyLimit() != null) {
            existing.setMonthlyLimit(dto.monthlyLimit());
        }

        if (dto.period() != null) {
            existing.setPeriod(dto.period());
        }

        return budgetLimitRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBudgetLimit(Long id) {
        if (!budgetLimitRepository.existsById(id)) {
            throw new BudgetNotFoundException(id);
        }

        budgetLimitRepository.deleteById(id);
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Category findActiveAvailableCategory(UUID categoryId, Long userId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId is required");
        }

        return categoryRepository.findActiveAvailableToUserById(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }
}
