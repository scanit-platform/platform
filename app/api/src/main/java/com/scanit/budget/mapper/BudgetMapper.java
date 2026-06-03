package com.scanit.budget.mapper;

import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(target = "category", source="budgetCategory.category")
    BudgetLimitResponseDTO toBudgetLimitResponse(BudgetLimit budgetLimit);
}
