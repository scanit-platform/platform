package com.scanit.budget.model;

import com.scanit.category.model.CustomCategory;
import com.scanit.category.model.GeneralCategory;
import com.scanit.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "budget_limit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_category_id", nullable = false)
    private GeneralCategory generalCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_category_id")
    private CustomCategory customCategory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private String period;
}
