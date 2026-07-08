package com.scanit.category.dto;

import java.util.UUID;

public record GeneralCategoryResponseDTO(
        UUID id,
        String code,
        String name,
        String icon,
        String color
) { }
