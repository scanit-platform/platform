package com.scanit.category.dto;

import com.scanit.category.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must be at most 100 characters")
        String name,

        @NotNull(message = "Category type is required")
        CategoryType type,

        @Size(max = 20, message = "Category icon must be at most 20 characters")
        String icon,

        @Size(max = 20, message = "Category color must be at most 20 characters")
        String color
) { }
