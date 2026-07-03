package com.scanit.category.dto;

import com.scanit.category.model.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        Long userId,
        String name,
        CategoryType type,
        String icon,
        String color,
        boolean system,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
