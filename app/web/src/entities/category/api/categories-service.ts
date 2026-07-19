import { ApiError, apiFetch } from "@/src/shared/api/client";
import { getAuthToken } from "@/src/features/auth/model/session";
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
  | "general_category_not_found"
  | "unauthorized";

export class CategoryServiceError extends Error {
  constructor(
    message: string,
    public readonly code: CategoryServiceErrorCode,
  ) {
    super(message);
    this.name = "CategoryServiceError";
  }
}

async function fetchCategoryJson<TResponse>(
  path: string,
  init: RequestInit = {},
): Promise<TResponse> {
    const token = await getAuthToken();

    if (!token) {
        throw new CategoryServiceError("Authentication is required", "Unauthorised");
    }

  try {
    return await apiFetch<TResponse>(path, init, {getAuthToken: token});
  } catch (error) {
    if (!(error instanceof ApiError)) {
      throw error;
    }

    throw new CategoryServiceError(
      error.message || "Category request failed.",
      error.status === 401 || error.status === 403
        ? "unauthorized"
        : "custom_category_not_found",
    );
  }
}

export function getGeneralCategories(): Promise<GeneralCategory[]> {
  return fetchCategoryJson<GeneralCategory[]>("/api/categories/general");
}

export function getCustomCategories(): Promise<CustomCategory[]> {
  return fetchCategoryJson<CustomCategory[]>("/api/categories/custom");
}

export function getCustomCategoriesByGeneralCategory(
  generalCategoryId: CategoryId,
): Promise<CustomCategory[]> {
  return fetchCategoryJson<CustomCategory[]>(
    `/api/categories/custom?generalCategoryId=${encodeURIComponent(
      generalCategoryId,
    )}`,
  );
}

export function createCustomCategory(
  input: CreateCustomCategoryInput,
): Promise<CustomCategory> {
  return fetchCategoryJson<CustomCategory>("/api/categories/custom", {
    body: JSON.stringify(input),
    method: "POST",
  });
}

export function updateCustomCategory(
  input: UpdateCustomCategoryInput,
): Promise<CustomCategory> {
  return fetchCategoryJson<CustomCategory>(
    `/api/categories/custom/${encodeURIComponent(input.id)}`,
    {
      body: JSON.stringify({
        name: input.name,
      }),
      method: "PUT",
    },
  );
}

export function deleteCustomCategory(id: CategoryId): Promise<void> {
  return fetchCategoryJson<void>(
    `/api/categories/custom/${encodeURIComponent(id)}`,
    {
      method: "DELETE",
    },
  );
}
