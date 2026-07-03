package com.scanit.category.repository;

import com.scanit.category.model.Category;
import com.scanit.category.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("""
            select c from Category c
            where c.active = true
              and (c.user is null or c.user.id = :userId)
              and (:type is null or c.type = :type)
            order by c.system desc, lower(c.name) asc
            """)
    List<Category> findActiveAvailableToUser(@Param("userId") Long userId, @Param("type") CategoryType type);

    @Query("""
            select c from Category c
            where c.id = :categoryId
              and c.active = true
              and (c.user is null or c.user.id = :userId)
            """)
    Optional<Category> findActiveAvailableToUserById(
            @Param("categoryId") UUID categoryId,
            @Param("userId") Long userId
    );

    @Query("""
            select count(c) > 0 from Category c
            where c.active = true
              and c.type = :type
              and lower(c.name) = lower(:name)
              and (c.user is null or c.user.id = :userId)
            """)
    boolean existsActiveAvailableByNameAndType(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("type") CategoryType type
    );

    @Query("""
            select count(c) > 0 from Category c
            where c.active = true
              and c.id <> :excludedId
              and c.type = :type
              and lower(c.name) = lower(:name)
              and (c.user is null or c.user.id = :userId)
            """)
    boolean existsActiveAvailableByNameAndTypeExcludingId(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("type") CategoryType type,
            @Param("excludedId") UUID excludedId
    );
}
