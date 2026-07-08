package com.scanit.category.controller;

import com.scanit.category.dto.CustomCategoryCreateRequestDTO;
import com.scanit.category.dto.CustomCategoryResponseDTO;
import com.scanit.category.dto.CustomCategoryUpdateRequestDTO;
import com.scanit.category.dto.GeneralCategoryResponseDTO;
import com.scanit.category.service.CategoryService;
import com.scanit.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/general")
    public List<GeneralCategoryResponseDTO> listGeneralCategories() {
        return categoryService.findAllGeneralCategories();
    }

    @GetMapping("/custom")
    @PreAuthorize("hasRole('USER')")
    public List<CustomCategoryResponseDTO> listCustomCategories(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) UUID generalCategoryId) {
        return categoryService.findCustomCategories(currentUser.getId(), generalCategoryId);
    }

    @PostMapping("/custom")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomCategoryResponseDTO> createCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CustomCategoryCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCustomCategory(currentUser.getId(), request));
    }

    @PutMapping("/custom/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CustomCategoryResponseDTO> renameCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CustomCategoryUpdateRequestDTO request) {
        return ResponseEntity.ok(categoryService.renameCustomCategory(currentUser.getId(), id, request));
    }

    @DeleteMapping("/custom/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        categoryService.deleteCustomCategory(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
