package com.scanit.budget.controller;

import com.scanit.budget.dto.BudgetLimitRequestDTO;
import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetLimit;
import com.scanit.budget.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin
public class BudgetController {
    private final BudgetService budgetService;

    private BudgetLimitResponseDTO toDTO(BudgetLimit limit){
        return new BudgetLimitResponseDTO(
                limit.getId(),
                limit.getBudgetCategory().getCategory(),
                limit.getMonthlyLimit(),
                limit.getPeriod()
        );
    }

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetLimitResponseDTO> createBudgetLimit(@RequestBody BudgetLimitRequestDTO dto) {

        return ResponseEntity.status(201)
                .body(budgetService.saveBudgetLimit(dto));
    }

    @GetMapping
    public List<BudgetLimitResponseDTO> getAll() {
        List<BudgetLimitResponseDTO> dtos = new ArrayList<>();
        Iterable<BudgetLimit> limits = budgetService.findAll();
        for (BudgetLimit limit : limits) {
            dtos.add(toDTO(limit));
        }
        return dtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetLimitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toDTO(budgetService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetLimitResponseDTO> updateBudgetLimit(@PathVariable Long id, @RequestBody BudgetLimitRequestDTO updated) {
        BudgetLimit saved = budgetService.updateBudgetLimit(id, updated);
        return ResponseEntity.ok(new BudgetLimitResponseDTO(
                saved.getId(),
                saved.getBudgetCategory().getCategory(),
                saved.getMonthlyLimit(),
                saved.getPeriod()
        ));
    }

    @DeleteMapping("/{id}")
    public void deleteBudgetLimit(@PathVariable Long id) {
        budgetService.deleteBudgetLimit(id);
    }

    @GetMapping("/search")
    public List<BudgetLimitResponseDTO> search(
            @RequestParam Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String period) {
        List<BudgetLimit> limits = budgetService.search(userId, categoryId, period);
        List<BudgetLimitResponseDTO> dtos = new ArrayList<>();

        for (BudgetLimit limit : limits) {
            dtos.add(new BudgetLimitResponseDTO(
                    limit.getId(),
                    limit.getBudgetCategory().getCategory(),
                    limit.getMonthlyLimit(),
                    limit.getPeriod()
            ));
        }

        return dtos;
    }
}
