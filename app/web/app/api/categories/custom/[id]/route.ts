import { ApiError, apiFetch } from "@/src/shared/api/client";
import { getAuthToken } from "@/src/features/auth/model/session";
import type { CustomCategory } from "@/src/entities/category/types/category";

type RouteContext = {
  params: Promise<{
    id: string;
  }>;
};

async function requireAuthToken() {
  const token = await getAuthToken();

  if (!token) {
    throw new ApiError("Sign in to manage custom categories.", 401);
  }

  return token;
}

export async function PUT(request: Request, context: RouteContext) {
  try {
    const token = await requireAuthToken();
    const { id } = await context.params;
    const payload = (await request.json()) as unknown;
    const category = await apiFetch<CustomCategory>(
      `/api/categories/custom/${encodeURIComponent(id)}`,
      {
        body: JSON.stringify(payload),
        method: "PUT",
      },
      { authToken: token },
    );

    return Response.json(category);
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "Unable to update custom category.";
    const status = error instanceof ApiError ? error.status ?? 500 : 500;

    return Response.json({ message }, { status });
  }
}

export async function DELETE(_request: Request, context: RouteContext) {
  try {
    const token = await requireAuthToken();
    const { id } = await context.params;

    await apiFetch<void>(
      `/api/categories/custom/${encodeURIComponent(id)}`,
      {
        method: "DELETE",
      },
      { authToken: token },
    );

    return new Response(null, { status: 204 });
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "Unable to delete custom category.";
    const status = error instanceof ApiError ? error.status ?? 500 : 500;

    return Response.json({ message }, { status });
  }
}
