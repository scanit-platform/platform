package com.scanit.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetLimitRequestDTO(
        @NotNull(message = "User id is required")
        Long userId,

        @NotNull(message = "General category id is required")
        UUID generalCategoryId,

        UUID customCategoryId,

        @NotNull(message = "Monthly limit is required")
        @Positive(message = "Monthly limit must be positive")
        BigDecimal monthlyLimit,

        @NotNull(message = "Period is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Period must use YYYY-MM format")
        String period
) { }
