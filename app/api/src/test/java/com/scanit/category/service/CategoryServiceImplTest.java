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
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
    private static final UUID GENERAL_CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOM_CATEGORY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private GeneralCategoryRepository generalCategoryRepository;

    @Mock
    private CustomCategoryRepository customCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryUsageChecker categoryUsageChecker;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        CategoryProperties categoryProperties = new CategoryProperties();
        categoryProperties.setMaxNameLength(100);
        categoryService = new CategoryServiceImpl(
                generalCategoryRepository,
                customCategoryRepository,
                userRepository,
                categoryMapper,
                categoryProperties,
                categoryUsageChecker);
    }

    @Test
    void shouldListGeneralCategories() {
        GeneralCategory food = generalCategory();
        List<GeneralCategory> categories = List.of(food);
        List<GeneralCategoryResponseDTO> responses = List.of(
                new GeneralCategoryResponseDTO(GENERAL_CATEGORY_ID, "FOOD_DINING", "Food & Dining", "utensils", "#2F855A"));

        when(generalCategoryRepository.findAllByOrderBySortOrderAscNameAsc()).thenReturn(categories);
        when(categoryMapper.toGeneralResponseList(categories)).thenReturn(responses);

        List<GeneralCategoryResponseDTO> actual = categoryService.findAllGeneralCategories();

        assertThat(actual).isEqualTo(responses);
    }

    @Test
    void shouldListCustomCategoriesForCurrentUserAndGeneralCategory() {
        CustomCategory customCategory = customCategory("Groceries");
        List<CustomCategory> categories = List.of(customCategory);
        List<CustomCategoryResponseDTO> responses = List.of(response(customCategory));

        when(generalCategoryRepository.existsById(GENERAL_CATEGORY_ID)).thenReturn(true);
        when(customCategoryRepository.findForUser(USER_ID, GENERAL_CATEGORY_ID)).thenReturn(categories);
        when(categoryMapper.toCustomResponseList(categories)).thenReturn(responses);

        List<CustomCategoryResponseDTO> actual = categoryService.findCustomCategories(USER_ID, GENERAL_CATEGORY_ID);

        assertThat(actual).isEqualTo(responses);
    }

    @Test
    void shouldCreateCustomCategoryForCurrentUser() {
        CustomCategoryCreateRequestDTO request = new CustomCategoryCreateRequestDTO(GENERAL_CATEGORY_ID, "  Groceries  ");
        User user = user(USER_ID);
        GeneralCategory generalCategory = generalCategory();
        CustomCategory saved = customCategory("Groceries");
        CustomCategoryResponseDTO response = response(saved);

        when(customCategoryRepository.existsForUserAndGeneralCategory(USER_ID, GENERAL_CATEGORY_ID, "groceries"))
                .thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(generalCategoryRepository.findById(GENERAL_CATEGORY_ID)).thenReturn(Optional.of(generalCategory));
        when(customCategoryRepository.save(any(CustomCategory.class))).thenReturn(saved);
        when(categoryMapper.toCustomResponse(saved)).thenReturn(response);

        CustomCategoryResponseDTO actual = categoryService.createCustomCategory(USER_ID, request);

        ArgumentCaptor<CustomCategory> captor = ArgumentCaptor.forClass(CustomCategory.class);
        verify(customCategoryRepository).save(captor.capture());
        CustomCategory persisted = captor.getValue();

        assertThat(actual).isEqualTo(response);
        assertThat(persisted.getUser()).isEqualTo(user);
        assertThat(persisted.getGeneralCategory()).isEqualTo(generalCategory);
        assertThat(persisted.getName()).isEqualTo("Groceries");
        assertThat(persisted.getNormalizedName()).isEqualTo("groceries");
    }

    @Test
    void shouldRejectDuplicateCustomCategoryWhenCreating() {
        CustomCategoryCreateRequestDTO request = new CustomCategoryCreateRequestDTO(GENERAL_CATEGORY_ID, "Groceries");

        when(customCategoryRepository.existsForUserAndGeneralCategory(USER_ID, GENERAL_CATEGORY_ID, "groceries"))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCustomCategory(USER_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Custom category already exists for this user and general category");

        verifyNoInteractions(userRepository, categoryMapper);
    }

    @Test
    void shouldRenameOwnedCustomCategory() {
        CustomCategory existing = customCategory("Groceries");
        CustomCategoryUpdateRequestDTO request = new CustomCategoryUpdateRequestDTO("  Weekly Groceries  ");
        CustomCategoryResponseDTO response = response(existing);

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.of(existing));
        when(customCategoryRepository.existsForUserAndGeneralCategoryExcludingId(
                USER_ID,
                GENERAL_CATEGORY_ID,
                "weekly groceries",
                CUSTOM_CATEGORY_ID)).thenReturn(false);
        when(customCategoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toCustomResponse(existing)).thenReturn(response);

        CustomCategoryResponseDTO actual = categoryService.renameCustomCategory(USER_ID, CUSTOM_CATEGORY_ID, request);

        assertThat(actual).isEqualTo(response);
        assertThat(existing.getName()).isEqualTo("Weekly Groceries");
        assertThat(existing.getNormalizedName()).isEqualTo("weekly groceries");
    }

    @Test
    void shouldTreatMissingOrUnownedCustomCategoryAsNotFound() {
        CustomCategoryUpdateRequestDTO request = new CustomCategoryUpdateRequestDTO("Food");

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.renameCustomCategory(USER_ID, CUSTOM_CATEGORY_ID, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Custom category with id " + CUSTOM_CATEGORY_ID + " not found");
    }

    @Test
    void shouldDeleteUnreferencedCustomCategory() {
        CustomCategory existing = customCategory("Groceries");

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.of(existing));
        when(categoryUsageChecker.isCustomCategoryReferenced(CUSTOM_CATEGORY_ID)).thenReturn(false);

        categoryService.deleteCustomCategory(USER_ID, CUSTOM_CATEGORY_ID);

        verify(customCategoryRepository).delete(existing);
    }

    @Test
    void shouldRejectDeletingReferencedCustomCategory() {
        CustomCategory existing = customCategory("Groceries");

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.of(existing));
        when(categoryUsageChecker.isCustomCategoryReferenced(CUSTOM_CATEGORY_ID)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCustomCategory(USER_ID, CUSTOM_CATEGORY_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Custom category is referenced and cannot be deleted");
    }

    @Test
    void shouldResolveCustomCategorySelection() {
        CustomCategory existing = customCategory("Groceries");

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.of(existing));

        CategorySelection selection = categoryService.requireSelection(USER_ID, GENERAL_CATEGORY_ID, CUSTOM_CATEGORY_ID);

        assertThat(selection.generalCategory()).isEqualTo(existing.getGeneralCategory());
        assertThat(selection.customCategory()).isEqualTo(existing);
    }

    @Test
    void shouldRejectCustomCategoryThatDoesNotBelongToSelectedGeneralCategory() {
        CustomCategory existing = customCategory("Groceries");
        UUID otherGeneralCategoryId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        when(customCategoryRepository.findOwnedById(CUSTOM_CATEGORY_ID, USER_ID)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.requireSelection(USER_ID, otherGeneralCategoryId, CUSTOM_CATEGORY_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Custom category does not belong to the selected general category");
    }

    private CustomCategory customCategory(String name) {
        CustomCategory customCategory = new CustomCategory();
        customCategory.setId(CUSTOM_CATEGORY_ID);
        customCategory.setUser(user(USER_ID));
        customCategory.setGeneralCategory(generalCategory());
        customCategory.rename(name);
        customCategory.setCreatedAt(Instant.now());
        customCategory.setUpdatedAt(Instant.now());
        return customCategory;
    }

    private GeneralCategory generalCategory() {
        return new GeneralCategory(GENERAL_CATEGORY_ID, "FOOD_DINING", "Food & Dining", "utensils", "#2F855A", 10);
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

    private CustomCategoryResponseDTO response(CustomCategory category) {
        return new CustomCategoryResponseDTO(
                category.getId(),
                category.getUser().getId(),
                category.getGeneralCategory().getId(),
                category.getGeneralCategory().getCode(),
                category.getGeneralCategory().getName(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
