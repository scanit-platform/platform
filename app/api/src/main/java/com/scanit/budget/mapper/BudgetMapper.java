package com.scanit.budget.mapper;

import com.scanit.budget.dto.BudgetLimitResponseDTO;
import com.scanit.budget.model.BudgetLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category.name")
    BudgetLimitResponseDTO toBudgetLimitResponse(BudgetLimit budgetLimit);
}
