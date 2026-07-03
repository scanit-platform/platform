package com.scanit.category.mapper;

import com.scanit.category.dto.CategoryResponseDTO;
import com.scanit.category.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "userId", source = "user.id")
    CategoryResponseDTO toResponse(Category category);

    List<CategoryResponseDTO> toResponseList(List<Category> categories);
}
