package com.scanit.category.service;

import com.scanit.category.config.CategoryProperties;
import com.scanit.category.dto.CustomCategoryCreateRequestDTO;
import com.scanit.category.dto.CustomCategoryResponseDTO;
import com.scanit.category.dto.CustomCategoryUpdateRequestDTO;
import com.scanit.category.dto.GeneralCategoryResponseDTO;
import com.scanit.category.mapper.CategoryMapper;
import com.scanit.category.model.CustomCategory;
import com.scanit.category.model.GeneralCategory;
import com.scanit.category.repository.CustomCategoryRepository;
import com.scanit.category.repository.GeneralCategoryRepository;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.NotFoundException;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService, CategoryReferenceService {
    private final GeneralCategoryRepository generalCategoryRepository;
    private final CustomCategoryRepository customCategoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryProperties categoryProperties;
    private final CategoryUsageChecker categoryUsageChecker;

    @Override
    public List<GeneralCategoryResponseDTO> findAllGeneralCategories() {
        return categoryMapper.toGeneralResponseList(generalCategoryRepository.findAllByOrderBySortOrderAscNameAsc());
    }

    @Override
    public List<CustomCategoryResponseDTO> findCustomCategories(Long userId, UUID generalCategoryId) {
        requireUserId(userId);

        if (generalCategoryId != null && !generalCategoryRepository.existsById(generalCategoryId)) {
            throw new NotFoundException("General category with id " + generalCategoryId + " not found");
        }

        return categoryMapper.toCustomResponseList(customCategoryRepository.findForUser(userId, generalCategoryId));
    }

    @Override
    @Transactional
    public CustomCategoryResponseDTO createCustomCategory(Long userId, CustomCategoryCreateRequestDTO request) {
        requireUserId(userId);
        String name = normalizeName(request.name());
        String normalizedName = normalizeForUniqueness(name);

        if (customCategoryRepository.existsForUserAndGeneralCategory(
                userId,
                request.generalCategoryId(),
                normalizedName)) {
            throw duplicateCategory();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        GeneralCategory generalCategory = findGeneralCategory(request.generalCategoryId());
        CustomCategory customCategory = new CustomCategory();
        customCategory.setUser(user);
        customCategory.setGeneralCategory(generalCategory);
        customCategory.rename(name);

        return categoryMapper.toCustomResponse(customCategoryRepository.save(customCategory));
    }

    @Override
    @Transactional
    public CustomCategoryResponseDTO renameCustomCategory(
            Long userId,
            UUID categoryId,
            CustomCategoryUpdateRequestDTO request) {
        CustomCategory customCategory = findOwnedCustomCategory(userId, categoryId);
        String name = normalizeName(request.name());
        String normalizedName = normalizeForUniqueness(name);

        if (customCategoryRepository.existsForUserAndGeneralCategoryExcludingId(
                userId,
                customCategory.getGeneralCategory().getId(),
                normalizedName,
                categoryId)) {
            throw duplicateCategory();
        }

        customCategory.rename(name);
        return categoryMapper.toCustomResponse(customCategoryRepository.save(customCategory));
    }

    @Override
    @Transactional
    public void deleteCustomCategory(Long userId, UUID categoryId) {
        CustomCategory customCategory = findOwnedCustomCategory(userId, categoryId);

        if (categoryUsageChecker.isCustomCategoryReferenced(categoryId)) {
            throw new BadRequestException("Custom category is referenced and cannot be deleted");
        }

        customCategoryRepository.delete(customCategory);
    }

    @Override
    public CategorySelection requireSelection(Long userId, UUID generalCategoryId, UUID customCategoryId) {
        CategorySelection selection = resolveOptionalSelection(userId, generalCategoryId, customCategoryId);

        if (selection.generalCategory() == null) {
            throw new BadRequestException("General category id is required");
        }

        return selection;
    }

    @Override
    public CategorySelection resolveOptionalSelection(Long userId, UUID generalCategoryId, UUID customCategoryId) {
        requireUserId(userId);

        if (customCategoryId != null) {
            CustomCategory customCategory = findOwnedCustomCategory(userId, customCategoryId);
            GeneralCategory customGeneralCategory = customCategory.getGeneralCategory();

            if (generalCategoryId != null && !customGeneralCategory.getId().equals(generalCategoryId)) {
                throw new BadRequestException("Custom category does not belong to the selected general category");
            }

            return new CategorySelection(customGeneralCategory, customCategory);
        }

        if (generalCategoryId == null) {
            return new CategorySelection(null, null);
        }

        return new CategorySelection(findGeneralCategory(generalCategoryId), null);
    }

    private CustomCategory findOwnedCustomCategory(Long userId, UUID categoryId) {
        requireUserId(userId);

        return customCategoryRepository.findOwnedById(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Custom category with id " + categoryId + " not found"));
    }

    private GeneralCategory findGeneralCategory(UUID generalCategoryId) {
        return generalCategoryRepository.findById(generalCategoryId)
                .orElseThrow(() -> new NotFoundException("General category with id " + generalCategoryId + " not found"));
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User id is required");
        }
    }

    private String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();

        if (normalized.isBlank()) {
            throw new BadRequestException("Category name is required");
        }

        if (normalized.length() > categoryProperties.getMaxNameLength()) {
            throw new BadRequestException(
                    "Category name must be at most " + categoryProperties.getMaxNameLength() + " characters");
        }

        return normalized;
    }

    private String normalizeForUniqueness(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private BadRequestException duplicateCategory() {
        return new BadRequestException("Custom category already exists for this user and general category");
    }
}
