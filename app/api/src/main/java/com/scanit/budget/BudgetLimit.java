package com.scanit.budget;

import com.scanit.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budget_limit")
public class BudgetLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private BudgetCategory budgetCategory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private String period;
}
