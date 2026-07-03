package com.scanit.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetLimitResponseDTO(
        Long id,
        Long userId,
        UUID categoryId,
        String category,
        BigDecimal monthlyLimit,
        String period
) { }
