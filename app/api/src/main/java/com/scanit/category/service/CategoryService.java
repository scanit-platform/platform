package com.scanit.category.service;

import com.scanit.category.dto.CustomCategoryCreateRequestDTO;
import com.scanit.category.dto.CustomCategoryResponseDTO;
import com.scanit.category.dto.CustomCategoryUpdateRequestDTO;
import com.scanit.category.dto.GeneralCategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<GeneralCategoryResponseDTO> findAllGeneralCategories();

    List<CustomCategoryResponseDTO> findCustomCategories(Long userId, UUID generalCategoryId);

    CustomCategoryResponseDTO createCustomCategory(Long userId, CustomCategoryCreateRequestDTO request);

    CustomCategoryResponseDTO renameCustomCategory(Long userId, UUID categoryId, CustomCategoryUpdateRequestDTO request);

    void deleteCustomCategory(Long userId, UUID categoryId);
}
