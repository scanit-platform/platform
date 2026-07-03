package com.scanit.category.service;

import com.scanit.category.dto.CategoryRequestDTO;
import com.scanit.category.dto.CategoryResponseDTO;
import com.scanit.category.mapper.CategoryMapper;
import com.scanit.category.model.Category;
import com.scanit.category.model.CategoryType;
import com.scanit.category.repository.CategoryRepository;
import com.scanit.exception.BadRequestException;
import com.scanit.exception.ForbiddenException;
import com.scanit.exception.NotFoundException;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    private static final Long USER_ID = 1L;
    private static final UUID CATEGORY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, userRepository, categoryMapper);
    }

    @Test
    void shouldListActiveCategoriesAvailableToUser() {
        Category groceries = category("Groceries", CategoryType.EXPENSE, null, true, true);
        Category custom = category("Pets", CategoryType.EXPENSE, user(), false, true);
        List<Category> categories = List.of(groceries, custom);
        List<CategoryResponseDTO> responses = List.of(response(groceries), response(custom));

        when(categoryRepository.findActiveAvailableToUser(USER_ID, CategoryType.EXPENSE)).thenReturn(categories);
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        List<CategoryResponseDTO> actual = categoryService.findActiveAvailableToUser(USER_ID, CategoryType.EXPENSE);

        assertThat(actual).isEqualTo(responses);
    }

    @Test
    void shouldCreateCustomCategoryForCurrentUser() {
        CategoryRequestDTO request = new CategoryRequestDTO("  Pets  ", CategoryType.EXPENSE, "  paw  ", " #111 ");
        User user = user();
        Category saved = category("Pets", CategoryType.EXPENSE, user, false, true);
        CategoryResponseDTO response = response(saved);

        when(categoryRepository.existsActiveAvailableByNameAndType(USER_ID, "Pets", CategoryType.EXPENSE))
                .thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryResponseDTO actual = categoryService.createCustomCategory(USER_ID, request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category persisted = categoryCaptor.getValue();

        assertThat(actual).isEqualTo(response);
        assertThat(persisted.getUser()).isEqualTo(user);
        assertThat(persisted.getName()).isEqualTo("Pets");
        assertThat(persisted.getType()).isEqualTo(CategoryType.EXPENSE);
        assertThat(persisted.getIcon()).isEqualTo("paw");
        assertThat(persisted.getColor()).isEqualTo("#111");
        assertThat(persisted.isSystem()).isFalse();
        assertThat(persisted.isActive()).isTrue();
    }

    @Test
    void shouldRejectDuplicateAvailableCategoryWhenCreating() {
        CategoryRequestDTO request = new CategoryRequestDTO("Groceries", CategoryType.EXPENSE, null, null);

        when(categoryRepository.existsActiveAvailableByNameAndType(USER_ID, "Groceries", CategoryType.EXPENSE))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCustomCategory(USER_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category already exists for this user");

        verifyNoInteractions(userRepository, categoryMapper);
    }

    @Test
    void shouldUpdateOwnedCustomCategory() {
        Category existing = category("Pets", CategoryType.EXPENSE, user(), false, true);
        existing.setId(CATEGORY_ID);
        CategoryRequestDTO request = new CategoryRequestDTO("Pet Care", CategoryType.EXPENSE, "pet", "blue");
        CategoryResponseDTO response = response(existing);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsActiveAvailableByNameAndTypeExcludingId(
                USER_ID,
                "Pet Care",
                CategoryType.EXPENSE,
                CATEGORY_ID)).thenReturn(false);
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toResponse(existing)).thenReturn(response);

        CategoryResponseDTO actual = categoryService.updateCustomCategory(USER_ID, CATEGORY_ID, request);

        assertThat(actual).isEqualTo(response);
        assertThat(existing.getName()).isEqualTo("Pet Care");
        assertThat(existing.getIcon()).isEqualTo("pet");
        assertThat(existing.getColor()).isEqualTo("blue");
    }

    @Test
    void shouldRejectModifyingSystemCategory() {
        Category systemCategory = category("Groceries", CategoryType.EXPENSE, null, true, true);
        systemCategory.setId(CATEGORY_ID);
        CategoryRequestDTO request = new CategoryRequestDTO("Food", CategoryType.EXPENSE, null, null);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.updateCustomCategory(USER_ID, CATEGORY_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("System categories cannot be modified");
    }

    @Test
    void shouldRejectModifyingAnotherUsersCategory() {
        Category otherUsersCategory = category("Pets", CategoryType.EXPENSE, user(2L), false, true);
        otherUsersCategory.setId(CATEGORY_ID);
        CategoryRequestDTO request = new CategoryRequestDTO("Pet Care", CategoryType.EXPENSE, null, null);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(otherUsersCategory));

        assertThatThrownBy(() -> categoryService.updateCustomCategory(USER_ID, CATEGORY_ID, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Category does not belong to the current user");
    }

    @Test
    void shouldSoftDeleteOwnedCustomCategory() {
        Category existing = category("Pets", CategoryType.EXPENSE, user(), false, true);
        existing.setId(CATEGORY_ID);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        categoryService.softDeleteCustomCategory(USER_ID, CATEGORY_ID);

        assertThat(existing.isActive()).isFalse();
        verify(categoryRepository).save(existing);
    }

    @Test
    void shouldTreatInactiveCategoryAsNotFoundWhenDeleting() {
        Category inactive = category("Pets", CategoryType.EXPENSE, user(), false, false);
        inactive.setId(CATEGORY_ID);

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> categoryService.softDeleteCustomCategory(USER_ID, CATEGORY_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id " + CATEGORY_ID + " not found");
    }

    private Category category(String name, CategoryType type, User user, boolean system, boolean active) {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setUser(user);
        category.setName(name);
        category.setType(type);
        category.setSystem(system);
        category.setActive(active);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    private User user() {
        return user(USER_ID);
    }

    private User user(Long id) {
        return new User(
                id,
                "Test User",
                "Test",
                "User",
                "test" + id + "@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    private CategoryResponseDTO response(Category category) {
        Long userId = category.getUser() == null ? null : category.getUser().getId();

        return new CategoryResponseDTO(
                category.getId(),
                userId,
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.isSystem(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
