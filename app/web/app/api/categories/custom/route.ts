import { ApiError, apiFetch } from "@/src/shared/api/client";
import { getAuthToken } from "@/src/features/auth/model/session";
import type { CustomCategory } from "@/src/entities/category/types/category";

async function requireAuthToken() {
  const token = await getAuthToken();

  if (!token) {
    throw new ApiError("Sign in to manage custom categories.", 401);
  }

  return token;
}

export async function GET(request: Request) {
  try {
    const token = await requireAuthToken();
    const { search } = new URL(request.url);
    const categories = await apiFetch<CustomCategory[]>(
      `/api/categories/custom${search}`,
      {
        method: "GET",
      },
      { authToken: token },
    );

    return Response.json(categories);
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "Unable to load custom categories.";
    const status = error instanceof ApiError ? error.status ?? 500 : 500;

    return Response.json({ message }, { status });
  }
}

export async function POST(request: Request) {
  try {
    const token = await requireAuthToken();
    const payload = (await request.json()) as unknown;
    const category = await apiFetch<CustomCategory>(
      "/api/categories/custom",
      {
        body: JSON.stringify(payload),
        method: "POST",
      },
      { authToken: token },
    );

    return Response.json(category, { status: 201 });
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "Unable to create custom category.";
    const status = error instanceof ApiError ? error.status ?? 500 : 500;

    return Response.json({ message }, { status });
  }
}
