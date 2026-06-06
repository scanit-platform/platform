package com.scanit.budget.exception;

public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(Long id) {
        super("Budget limit with id " + id + " not found");
    }
}
