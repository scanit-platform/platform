package com.scanit.budget.dto;

import java.math.BigDecimal;

public record BudgetLimitRequestDTO(
        Long categoryId,
        BigDecimal monthlyLimit,
        String period
) { }
