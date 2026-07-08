import {
  generalCategories,
  initialCustomCategories,
} from "@/src/entities/category/model/category-reference-data";
import type {
  CategoryId,
  CreateCustomCategoryInput,
  CustomCategory,
  GeneralCategory,
  UpdateCustomCategoryInput,
} from "@/src/entities/category/types/category";

export type CategoryServiceErrorCode =
  | "custom_category_duplicate"
  | "custom_category_not_found"
  | "general_category_not_found";

export class CategoryServiceError extends Error {
  constructor(
    message: string,
    public readonly code: CategoryServiceErrorCode,
  ) {
    super(message);
    this.name = "CategoryServiceError";
  }
}

let customCategories = [...initialCustomCategories];

const mockNetworkDelayMs = 180;

function delay<T>(value: T): Promise<T> {
  return new Promise((resolve) => {
    setTimeout(() => resolve(value), mockNetworkDelayMs);
  });
}

function cloneGeneralCategory(category: GeneralCategory): GeneralCategory {
  return { ...category };
}

function cloneCustomCategory(category: CustomCategory): CustomCategory {
  return { ...category };
}

function normalizeCategoryName(name: string) {
  return name.trim().toLocaleLowerCase();
}

function sortCustomCategories(categories: readonly CustomCategory[]) {
  return [...categories].sort((left, right) =>
    left.name.localeCompare(right.name),
  );
}

function assertGeneralCategoryExists(generalCategoryId: CategoryId) {
  const exists = generalCategories.some(
    (category) => category.id === generalCategoryId,
  );

  if (!exists) {
    throw new CategoryServiceError(
      "General category does not exist.",
      "general_category_not_found",
    );
  }
}

function assertUniqueCustomCategoryName({
  excludedCustomCategoryId,
  generalCategoryId,
  name,
}: {
  excludedCustomCategoryId?: CategoryId;
  generalCategoryId: CategoryId;
  name: string;
}) {
  const normalizedName = normalizeCategoryName(name);
  const exists = customCategories.some((category) => {
    return (
      category.generalCategoryId === generalCategoryId &&
      category.id !== excludedCustomCategoryId &&
      normalizeCategoryName(category.name) === normalizedName
    );
  });

  if (exists) {
    throw new CategoryServiceError(
      "A custom category with this name already exists in the selected general category.",
      "custom_category_duplicate",
    );
  }
}

function createCustomCategoryId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }

  return `custom-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export async function getGeneralCategories(): Promise<GeneralCategory[]> {
  return delay(generalCategories.map(cloneGeneralCategory));
}

export async function getCustomCategories(): Promise<CustomCategory[]> {
  return delay(sortCustomCategories(customCategories).map(cloneCustomCategory));
}

export async function getCustomCategoriesByGeneralCategory(
  generalCategoryId: CategoryId,
): Promise<CustomCategory[]> {
  assertGeneralCategoryExists(generalCategoryId);

  return delay(
    sortCustomCategories(
      customCategories.filter(
        (category) => category.generalCategoryId === generalCategoryId,
      ),
    ).map(cloneCustomCategory),
  );
}

export async function createCustomCategory(
  input: CreateCustomCategoryInput,
): Promise<CustomCategory> {
  const name = input.name.trim();

  assertGeneralCategoryExists(input.generalCategoryId);
  assertUniqueCustomCategoryName({
    generalCategoryId: input.generalCategoryId,
    name,
  });

  const createdCategory: CustomCategory = {
    generalCategoryId: input.generalCategoryId,
    id: createCustomCategoryId(),
    name,
  };

  customCategories = [...customCategories, createdCategory];

  return delay(cloneCustomCategory(createdCategory));
}

export async function updateCustomCategory(
  input: UpdateCustomCategoryInput,
): Promise<CustomCategory> {
  const category = customCategories.find(
    (customCategory) => customCategory.id === input.id,
  );

  if (!category) {
    throw new CategoryServiceError(
      "Custom category does not exist.",
      "custom_category_not_found",
    );
  }

  const nextGeneralCategoryId =
    input.generalCategoryId ?? category.generalCategoryId;
  const nextName = input.name?.trim() ?? category.name;

  assertGeneralCategoryExists(nextGeneralCategoryId);
  assertUniqueCustomCategoryName({
    excludedCustomCategoryId: input.id,
    generalCategoryId: nextGeneralCategoryId,
    name: nextName,
  });

  const updatedCategory: CustomCategory = {
    ...category,
    generalCategoryId: nextGeneralCategoryId,
    name: nextName,
  };

  customCategories = customCategories.map((customCategory) =>
    customCategory.id === updatedCategory.id ? updatedCategory : customCategory,
  );

  return delay(cloneCustomCategory(updatedCategory));
}

export async function deleteCustomCategory(id: CategoryId): Promise<void> {
  const exists = customCategories.some((category) => category.id === id);

  if (!exists) {
    throw new CategoryServiceError(
      "Custom category does not exist.",
      "custom_category_not_found",
    );
  }

  customCategories = customCategories.filter((category) => category.id !== id);

  return delay(undefined);
}
