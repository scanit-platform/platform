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

function isStrongEnoughPassword(password: string) {
  return password.length >= 8 && /[A-Za-z]/.test(password) && /\d/.test(password);
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

  const firstName = String(formData.get("firstName") ?? "").trim();
  const lastName = String(formData.get("lastName") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "").trim();
  const confirmPassword = String(formData.get("confirmPassword") ?? "").trim();

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
    if (!firstName) {
      fieldErrors.firstName = "First name is required.";
    }

    if (!lastName) {
      fieldErrors.lastName = "Last name is required.";
    }

    if (!confirmPassword) {
      fieldErrors.confirmPassword = "Confirm password is required.";
    }

    if (password && !isStrongEnoughPassword(password)) {
      fieldErrors.password =
          "Password must be at least 8 characters and include one letter and one number.";
    }

    if (password && confirmPassword && password !== confirmPassword) {
      fieldErrors.confirmPassword = "Passwords do not match.";
    }
  }

  if (Object.keys(fieldErrors).length > 0) {
    return {
      message: "Check the highlighted fields and try again.",
      fieldErrors,
    };
  }

  let redirectPath = "";

  try {
    if (mode === "signup") {
      await register({
        firstName,
        lastName,
        email,
        password,
        confirmPassword,
      });

      const response = await login({ email, password });
      const tokenErrorMessage = await storeAuthToken(response);

      if (tokenErrorMessage) {
        return {
          ...initialAuthState,
          message: tokenErrorMessage,
        };
      }

      redirectPath = "/dashboard?mode=signup";
    } else {
      const response = await login({ email, password });
      const tokenErrorMessage = await storeAuthToken(response);

      if (tokenErrorMessage) {
        return {
          ...initialAuthState,
          message: tokenErrorMessage,
        };
      }

      redirectPath = "/dashboard?mode=signin";
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

  redirect(redirectPath);
}
