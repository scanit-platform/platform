package com.scanit.receipt.model;

import com.scanit.budget.BudgetCategory;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "receipt_line_item")
public class ReceiptLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private Integer quantity;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "receipt_id")
    private Receipt receipt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private BudgetCategory category;
}
