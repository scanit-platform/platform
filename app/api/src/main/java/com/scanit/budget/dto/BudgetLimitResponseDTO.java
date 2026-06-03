package com.scanit.budget.dto;

import java.math.BigDecimal;

public record BudgetLimitResponseDTO(
        Long id,
        String category,
        BigDecimal monthlyLimit,
        String period
) { }
