package com.scanit.category.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "general_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GeneralCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50, updatable = false)
    private String code;

    @Column(nullable = false, length = 100, updatable = false)
    private String name;

    @Column(length = 50, updatable = false)
    private String icon;

    @Column(length = 20, updatable = false)
    private String color;

    @Column(name = "sort_order", nullable = false, updatable = false)
    private int sortOrder;
}
