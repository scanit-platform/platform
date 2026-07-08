package com.scanit.category.mapper;

import com.scanit.category.dto.CustomCategoryResponseDTO;
import com.scanit.category.dto.GeneralCategoryResponseDTO;
import com.scanit.category.model.CustomCategory;
import com.scanit.category.model.GeneralCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    GeneralCategoryResponseDTO toGeneralResponse(GeneralCategory generalCategory);

    List<GeneralCategoryResponseDTO> toGeneralResponseList(List<GeneralCategory> generalCategories);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "generalCategoryId", source = "generalCategory.id")
    @Mapping(target = "generalCategoryCode", source = "generalCategory.code")
    @Mapping(target = "generalCategoryName", source = "generalCategory.name")
    CustomCategoryResponseDTO toCustomResponse(CustomCategory customCategory);

    List<CustomCategoryResponseDTO> toCustomResponseList(List<CustomCategory> customCategories);
}
