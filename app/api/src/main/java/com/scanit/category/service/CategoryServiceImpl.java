package com.scanit.category.service;

import com.scanit.category.dto.CategoryRequestDTO;
import com.scanit.category.dto.CategoryResponseDTO;
import com.scanit.category.mapper.CategoryMapper;
import com.scanit.category.model.CategoryType;
import com.scanit.category.model.Category;
import com.scanit.category.repository.CategoryRepository;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.ForbiddenException;
import com.scanit.exception.NotFoundException;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDTO> findActiveAvailableToUser(Long userId, CategoryType type) {
        return categoryMapper.toResponseList(categoryRepository.findActiveAvailableToUser(userId, type));
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCustomCategory(Long userId, CategoryRequestDTO request) {
        validateUniqueAvailableCategory(userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        Category category = new Category();
        category.setUser(user);
        category.setName(normalizeName(request.name()));
        category.setType(request.type());
        category.setIcon(normalizeNullable(request.icon()));
        category.setColor(normalizeNullable(request.color()));
        category.setSystem(false);
        category.setActive(true);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCustomCategory(Long userId, UUID categoryId, CategoryRequestDTO request) {
        Category category = findOwnedCustomCategory(userId, categoryId);

        if (categoryRepository.existsActiveAvailableByNameAndTypeExcludingId(
                userId,
                normalizeName(request.name()),
                request.type(),
                categoryId)) {
            throw new BadRequestException("Category already exists for this user");
        }

        category.setName(normalizeName(request.name()));
        category.setType(request.type());
        category.setIcon(normalizeNullable(request.icon()));
        category.setColor(normalizeNullable(request.color()));

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void softDeleteCustomCategory(Long userId, UUID categoryId) {
        Category category = findOwnedCustomCategory(userId, categoryId);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findOwnedCustomCategory(Long userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id " + categoryId + " not found"));

        if (category.isSystem() || category.getUser() == null) {
            throw new ForbiddenException("System categories cannot be modified");
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Category does not belong to the current user");
        }

        if (!category.isActive()) {
            throw new NotFoundException("Category with id " + categoryId + " not found");
        }

        return category;
    }

    private void validateUniqueAvailableCategory(Long userId, CategoryRequestDTO request) {
        if (categoryRepository.existsActiveAvailableByNameAndType(
                userId,
                normalizeName(request.name()),
                request.type())) {
            throw new BadRequestException("Category already exists for this user");
        }
    }

    private String normalizeName(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
