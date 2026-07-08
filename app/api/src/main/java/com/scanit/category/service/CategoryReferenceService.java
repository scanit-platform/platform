package com.scanit.category.service;

import java.util.UUID;

public interface CategoryReferenceService {
    CategorySelection requireSelection(Long userId, UUID generalCategoryId, UUID customCategoryId);

    CategorySelection resolveOptionalSelection(Long userId, UUID generalCategoryId, UUID customCategoryId);
}
