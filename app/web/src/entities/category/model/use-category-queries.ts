"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getCustomCategories,
  getCustomCategoriesByGeneralCategory,
  getGeneralCategories,
} from "@/src/entities/category/api/categories-service";
import { categoryQueryKeys } from "@/src/entities/category/model/category-query-keys";
import type { CategoryId } from "@/src/entities/category/types/category";

export function useGeneralCategoriesQuery() {
  return useQuery({
    queryFn: getGeneralCategories,
    queryKey: categoryQueryKeys.generalCategories(),
  });
}

export function useCustomCategoriesQuery() {
  return useQuery({
    queryFn: getCustomCategories,
    queryKey: categoryQueryKeys.customCategories(),
  });
}

export function useCustomCategoriesByGeneralCategoryQuery(
  generalCategoryId: CategoryId | undefined,
) {
  return useQuery({
    enabled: Boolean(generalCategoryId),
    queryFn: () =>
      generalCategoryId
        ? getCustomCategoriesByGeneralCategory(generalCategoryId)
        : Promise.resolve([]),
    queryKey: categoryQueryKeys.customCategoriesByGeneralCategory(
      generalCategoryId ?? "",
    ),
  });
}
