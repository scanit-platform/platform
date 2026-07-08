package com.scanit.category.repository;

import com.scanit.category.model.CustomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomCategoryRepository extends JpaRepository<CustomCategory, UUID> {
    @Query("""
            select c from CustomCategory c
            join fetch c.generalCategory
            where c.user.id = :userId
              and (:generalCategoryId is null or c.generalCategory.id = :generalCategoryId)
            order by c.generalCategory.sortOrder asc, lower(c.name) asc
            """)
    List<CustomCategory> findForUser(
            @Param("userId") Long userId,
            @Param("generalCategoryId") UUID generalCategoryId
    );

    @Query("""
            select c from CustomCategory c
            join fetch c.generalCategory
            where c.id = :categoryId
              and c.user.id = :userId
            """)
    Optional<CustomCategory> findOwnedById(
            @Param("categoryId") UUID categoryId,
            @Param("userId") Long userId
    );

    @Query("""
            select count(c) > 0 from CustomCategory c
            where c.user.id = :userId
              and c.generalCategory.id = :generalCategoryId
              and c.normalizedName = :normalizedName
            """)
    boolean existsForUserAndGeneralCategory(
            @Param("userId") Long userId,
            @Param("generalCategoryId") UUID generalCategoryId,
            @Param("normalizedName") String normalizedName
    );

    @Query("""
            select count(c) > 0 from CustomCategory c
            where c.user.id = :userId
              and c.generalCategory.id = :generalCategoryId
              and c.normalizedName = :normalizedName
              and c.id <> :excludedId
            """)
    boolean existsForUserAndGeneralCategoryExcludingId(
            @Param("userId") Long userId,
            @Param("generalCategoryId") UUID generalCategoryId,
            @Param("normalizedName") String normalizedName,
            @Param("excludedId") UUID excludedId
    );
}
