import type { CategoryId } from "@/src/entities/category/types/category";

export const categoryQueryKeys = {
  all: ["categories"] as const,
  customCategories: () =>
    [...categoryQueryKeys.all, "custom-categories"] as const,
  customCategoriesByGeneralCategory: (generalCategoryId: CategoryId) =>
    [
      ...categoryQueryKeys.customCategories(),
      "by-general-category",
      generalCategoryId,
    ] as const,
  generalCategories: () =>
    [...categoryQueryKeys.all, "general-categories"] as const,
};
