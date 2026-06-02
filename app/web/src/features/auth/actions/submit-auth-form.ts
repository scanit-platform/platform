"use server";

import { redirect } from "next/navigation";
import {
  initialAuthState,
  type AuthActionState,
} from "@/src/features/auth/model/auth-state";

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
  const firstName = String(formData.get("firstName") ?? "").trim();
  const lastName = String(formData.get("lastName") ?? "").trim();
  const countryCode = String(formData.get("countryCode") ?? "").trim();
  const localPhoneDigits = String(formData.get("phone") ?? "").replace(/\D/g, "");
  const fullPhone = `${countryCode}${localPhoneDigits}`;

  const fieldErrors: AuthActionState["fieldErrors"] = {};

  if (!email) {
    fieldErrors.email = "Email is required.";
  } else if (!isValidEmail(email)) {
    fieldErrors.email = "Enter a valid email address.";
  }

  if (mode === "signin") {
    if (!password) {
      fieldErrors.password = "Password is required.";
    }
  }

  if (mode === "signup") {
    if (!firstName) {
      fieldErrors.firstName = "First name is required.";
    }

    if (!lastName) {
      fieldErrors.lastName = "Last name is required.";
    }

    if (!localPhoneDigits) {
      fieldErrors.phone = "Phone is required.";
    } else if (fullPhone.length < 10) {
      fieldErrors.phone = "Enter a valid phone number.";
    }
  }

  if (Object.keys(fieldErrors).length > 0) {
    return {
      message: "Check the highlighted fields and try again.",
      fieldErrors,
    };
  }

  redirect(`/dashboard?mode=${mode}`);
}
