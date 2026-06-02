import { apiFetch } from "@/src/shared/api/client";
import type { User } from "@/src/entities/user/types/user";

export function getCurrentUser(token: string) {
  return apiFetch<User>("/auth/me", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
}
