import { getAuthToken } from "@/src/features/auth/model/session";
import { apiFetch } from "@/src/shared/api/client";
import type { CustomCategory, CreateCustomCategoryInput } from "@/src/entities/category/types/category";

export async function getCustomCategoriesForUser() {
  const token = await getAuthToken();

  if (!token) throw new Error("Authentication is required.");

  return apiFetch<CustomCategory[]>(
    "/api/categories/custom",
    { method: "GET" },
    { authToken: token },
  );
}

export async function createCustomCategoryForUser(
    input: CreateCustomCategoryInput,
) {
    const token = await getAuthToken()
    if (!token) throw new Error("Authentication is required.");

    return apiFetch<CustomCategory>(
        "/api/categories/custom",
     {
        method: "POST",
        body: JSON.stringify(input),
      },
        { authToken: token },
    )
  }