package com.scanit.budget.controller;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.mapper.BudgetMapper;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.service.BudgetService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin
public class BudgetController {
    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring-managed collaborators are intentionally stored for constructor injection."
    )
    public BudgetController(BudgetService budgetService, BudgetMapper budgetMapper) {
        this.budgetService = budgetService;
        this.budgetMapper = budgetMapper;
    }

    @PostMapping
    public ResponseEntity<BudgetLimitResponseDTO> createBudgetLimit(@Valid @RequestBody BudgetLimitRequestDTO dto) {
        return ResponseEntity.status(201)
                .body(budgetService.saveBudgetLimit(dto));
    }

    @GetMapping
    public List<BudgetLimitResponseDTO> getAll() {
        List<BudgetLimitResponseDTO> dtos = new ArrayList<>();
        Iterable<BudgetLimit> limits = budgetService.findAll();
        for (BudgetLimit limit : limits) {
            dtos.add(budgetMapper.toBudgetLimitResponse(limit));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetLimitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetMapper.toBudgetLimitResponse(budgetService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetLimitResponseDTO> updateBudgetLimit(
            @PathVariable Long id,
            @RequestBody BudgetLimitRequestDTO updated) {
        BudgetLimit saved = budgetService.updateBudgetLimit(id, updated);
        return ResponseEntity.ok(budgetMapper.toBudgetLimitResponse(saved));
    }

    @DeleteMapping("/{id}")
    public void deleteBudgetLimit(@PathVariable Long id) {
        budgetService.deleteBudgetLimit(id);
    }

    @GetMapping("/search")
    public List<BudgetLimitResponseDTO> search(
            @RequestParam Long userId,
            @RequestParam(required = false) UUID generalCategoryId,
            @RequestParam(required = false) UUID customCategoryId,
            @RequestParam(required = false) String period) {
        List<BudgetLimit> limits = budgetService.search(userId, generalCategoryId, customCategoryId, period);
        List<BudgetLimitResponseDTO> dtos = new ArrayList<>();

        for (BudgetLimit limit : limits) {
            dtos.add(budgetMapper.toBudgetLimitResponse(limit));
        }

        return dtos;
    }
}
