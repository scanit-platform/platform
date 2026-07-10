import { ApiError, apiFetch } from "@/src/shared/api/client";
import type { GeneralCategory } from "@/src/entities/category/types/category";

export async function GET() {
  try {
    const categories = await apiFetch<GeneralCategory[]>(
      "/api/categories/general",
      {
        method: "GET",
      },
    );

    return Response.json(categories);
  } catch (error) {
    const message =
      error instanceof Error ? error.message : "Unable to load categories.";
    const status = error instanceof ApiError ? error.status ?? 500 : 500;

    return Response.json({ message }, { status });
  }
}
