package com.scanit.category.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomCategoryResponseDTO(
        UUID id,
        Long userId,
        UUID generalCategoryId,
        String generalCategoryCode,
        String generalCategoryName,
        String name,
        Instant createdAt,
        Instant updatedAt
) { }
