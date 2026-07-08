package com.scanit.category.repository;

import com.scanit.category.model.GeneralCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneralCategoryRepository extends JpaRepository<GeneralCategory, UUID> {
    List<GeneralCategory> findAllByOrderBySortOrderAscNameAsc();
}
