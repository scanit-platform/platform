"use server";

import { redirect } from "next/navigation";
import { login, register } from "@/src/features/auth/api/auth-api";
import {
  initialAuthState,
  type AuthActionState,
} from "@/src/features/auth/model/auth-state";
import { storeAuthToken } from "@/src/features/auth/model/session";

function isValidEmail(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export async function submitAuthForm(
  _previousState: AuthActionState,
  formData: FormData,
): Promise<AuthActionState> {
  const mode = formData.get("mode");

  if (mode !== "signup" && mode !== "signin") {
    return {
      ...initialAuthState,
      message: "The selected authentication mode is invalid.",
    };
  }

  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "").trim();
  const name = String(formData.get("name") ?? "").trim();

  const fieldErrors: AuthActionState["fieldErrors"] = {};

  if (!email) {
    fieldErrors.email = "Email is required.";
  } else if (!isValidEmail(email)) {
    fieldErrors.email = "Enter a valid email address.";
  }

  if (!password) {
    fieldErrors.password = "Password is required.";
  }

  if (mode === "signup") {
    if (!name) {
      fieldErrors.name = "Name is required.";
    }

    if (password && password.length < 6) {
      fieldErrors.password = "Password must be at least 6 characters.";
    }
  }

  if (Object.keys(fieldErrors).length > 0) {
    return {
      message: "Check the highlighted fields and try again.",
      fieldErrors,
    };
  }

  try {
    const response =
      mode === "signup"
        ? await register({ name, email, password })
        : await login({ email, password });
    const tokenErrorMessage = await storeAuthToken(response);

    if (tokenErrorMessage) {
      return {
        ...initialAuthState,
        message: tokenErrorMessage,
      };
    }
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "Authentication failed. Try again.";

    return {
      ...initialAuthState,
      message,
    };
  }

  redirect(`/dashboard?mode=${mode}`);
}
