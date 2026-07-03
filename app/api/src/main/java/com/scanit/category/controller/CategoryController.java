package com.scanit.category.controller;

import com.scanit.category.dto.CategoryRequestDTO;
import com.scanit.category.dto.CategoryResponseDTO;
import com.scanit.category.model.CategoryType;
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
@PreAuthorize("hasRole('USER')")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDTO> listActiveCategories(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) CategoryType type) {
        return categoryService.findActiveAvailableToUser(currentUser.getId(), type);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCustomCategory(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(categoryService.updateCustomCategory(currentUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomCategory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        categoryService.softDeleteCustomCategory(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
