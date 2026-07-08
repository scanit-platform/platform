package com.scanit.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetLimitResponseDTO(
        Long id,
        Long userId,
        UUID generalCategoryId,
        UUID customCategoryId,
        BigDecimal monthlyLimit,
        String period
) { }
