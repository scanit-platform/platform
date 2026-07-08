package com.scanit.budget.service;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.exception.BudgetNotFoundException;
import com.scanit.budget.mapper.BudgetMapper;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.repository.BudgetLimitRepository;
import com.scanit.category.service.CategoryReferenceService;
import com.scanit.category.service.CategorySelection;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetServiceImpl implements BudgetService {
    private final BudgetLimitRepository budgetLimitRepository;
    private final CategoryReferenceService categoryReferenceService;
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
    public List<BudgetLimit> search(Long userId, UUID generalCategoryId, UUID customCategoryId, String period) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        return budgetLimitRepository.search(userId, generalCategoryId, customCategoryId, period);
    }

    @Override
    @Transactional
    public BudgetLimitResponseDTO saveBudgetLimit(BudgetLimitRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Budget limit request is required");
        }

        User user = findUser(dto.userId());
        CategorySelection categorySelection = categoryReferenceService.requireSelection(
                dto.userId(),
                dto.generalCategoryId(),
                dto.customCategoryId());

        BudgetLimit limit = new BudgetLimit();
        limit.setUser(user);
        limit.setGeneralCategory(categorySelection.generalCategory());
        limit.setCustomCategory(categorySelection.customCategory());
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

        if (dto.generalCategoryId() != null || dto.customCategoryId() != null) {
            Long ownerId = existing.getUser().getId();
            CategorySelection categorySelection = categoryReferenceService.requireSelection(
                    ownerId,
                    dto.generalCategoryId(),
                    dto.customCategoryId());
            existing.setGeneralCategory(categorySelection.generalCategory());
            existing.setCustomCategory(categorySelection.customCategory());
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

}
