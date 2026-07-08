package com.scanit.category.dto;

import com.scanit.category.validation.ValidCategoryName;

public record CustomCategoryUpdateRequestDTO(
        @ValidCategoryName
        String name
) { }
