package com.scanit.category.dto;

import com.scanit.category.validation.ValidCategoryName;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomCategoryCreateRequestDTO(
        @NotNull(message = "General category id is required")
        UUID generalCategoryId,

        @ValidCategoryName
        String name
) { }
