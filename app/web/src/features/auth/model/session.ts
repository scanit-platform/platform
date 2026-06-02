import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { ApiError } from "@/src/shared/api/client";
import { getCurrentUser } from "@/src/entities/user/api/get-current-user";
import type { User } from "@/src/entities/user/types/user";
import type { AuthResponse } from "@/src/features/auth/types/auth";

const authTokenCookieName = "scanit_auth_token";

export async function storeAuthToken(response: AuthResponse) {
  if (!response.token) {
    return "Authentication succeeded, but the server did not return a token.";
  }

  const cookieStore = await cookies();
  const expires = response.expiresAt ? new Date(response.expiresAt) : undefined;

  cookieStore.set(authTokenCookieName, response.token, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    ...(expires && !Number.isNaN(expires.getTime()) ? { expires } : {}),
  });

  return undefined;
}

export async function clearAuthToken() {
  const cookieStore = await cookies();

  cookieStore.delete(authTokenCookieName);
}

export async function getAuthToken() {
  const cookieStore = await cookies();

  return cookieStore.get(authTokenCookieName)?.value;
}

export async function getAuthenticatedUser(): Promise<User | null> {
  const token = await getAuthToken();

  if (!token) {
    return null;
  }

  try {
    return await getCurrentUser(token);
  } catch (error) {
    if (
      error instanceof ApiError &&
      (error.status === 401 || error.status === 403)
    ) {
      return null;
    }

    throw error;
  }
}

export async function getAuthenticatedUserOrRedirect(): Promise<User> {
  const user = await getAuthenticatedUser();

  if (!user) {
    redirect("/login");
  }

  return user;
}
