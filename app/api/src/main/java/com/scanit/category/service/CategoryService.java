package com.scanit.category.service;

import com.scanit.category.dto.CategoryRequestDTO;
import com.scanit.category.dto.CategoryResponseDTO;
import com.scanit.category.model.CategoryType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponseDTO> findActiveAvailableToUser(Long userId, CategoryType type);

    CategoryResponseDTO createCustomCategory(Long userId, CategoryRequestDTO request);

    CategoryResponseDTO updateCustomCategory(Long userId, UUID categoryId, CategoryRequestDTO request);

    void softDeleteCustomCategory(Long userId, UUID categoryId);
}
