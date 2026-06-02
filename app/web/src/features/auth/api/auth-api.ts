import { apiFetch } from "@/src/shared/api/client";
import type {
  AuthRequest,
  AuthResponse,
  RegisterRequest,
} from "@/src/features/auth/types/auth";

function getErrorMessage(status: number) {
  if (status === 401) {
    return "The email or password is incorrect.";
  }

  if (status === 409) {
    return "An account with this email already exists.";
  }

  return undefined;
}

export function register(payload: RegisterRequest) {
  return apiFetch<AuthResponse>(
    "/auth/register",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    { getErrorMessage },
  );
}

export function login(payload: AuthRequest) {
  return apiFetch<AuthResponse>(
    "/auth/login",
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    { getErrorMessage },
  );
}
