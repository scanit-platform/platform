package com.scanit.category.service;

import com.scanit.budget.repository.BudgetLimitRepository;
import com.scanit.receipt.repository.ReceiptLineItemRepository;
import com.scanit.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryUsageCheckerImpl implements CategoryUsageChecker {
    private final ReceiptRepository receiptRepository;
    private final ReceiptLineItemRepository receiptLineItemRepository;
    private final BudgetLimitRepository budgetLimitRepository;

    @Override
    public boolean isCustomCategoryReferenced(UUID customCategoryId) {
        return receiptRepository.existsByCustomCategoryId(customCategoryId)
                || receiptLineItemRepository.existsByCustomCategoryId(customCategoryId)
                || budgetLimitRepository.existsByCustomCategoryId(customCategoryId);
    }
}
