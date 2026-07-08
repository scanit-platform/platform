package com.scanit.category.service;

import java.util.UUID;

public interface CategoryUsageChecker {
    boolean isCustomCategoryReferenced(UUID customCategoryId);
}
