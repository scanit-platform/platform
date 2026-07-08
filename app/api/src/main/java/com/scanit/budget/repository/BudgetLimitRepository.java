package com.scanit.budget.repository;

import com.scanit.budget.model.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByUserId(Long userId);

    @Query("""
            select b from BudgetLimit b
            where b.user.id = :userId
              and (:generalCategoryId is null or b.generalCategory.id = :generalCategoryId)
              and (:customCategoryId is null or b.customCategory.id = :customCategoryId)
              and (:period is null or b.period = :period)
            """)
    List<BudgetLimit> search(
            @Param("userId") Long userId,
            @Param("generalCategoryId") UUID generalCategoryId,
            @Param("customCategoryId") UUID customCategoryId,
            @Param("period") String period
    );

    boolean existsByCustomCategoryId(UUID customCategoryId);
}
