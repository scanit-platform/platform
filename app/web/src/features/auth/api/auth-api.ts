import { apiFetch } from "@/src/shared/api/client";
import type {
  AuthRequest,
  AuthResponse,
  RegisterRequest,
  RegistrationResponse,
  ResendVerificationRequest,
} from "@/src/features/auth/types/auth";

function getErrorMessage(status: number) {
  if (status === 401) {
    return "The email or password is incorrect.";
  }

  if (status === 403) {
      return "Your account isn't verified. Check your email first.";
  }

  if (status === 409) {
    return "An account with this email already exists.";
  }

  return undefined;
}

export function register(payload: RegisterRequest){
   return apiFetch<RegistrationResponse>(
       "/auth/register",
       {
           method: "POST",
           body: JSON.stringify(payload),
       },
       {getErrorMessage}
   );
}

export function login(payload: AuthRequest){
    return apiFetch<AuthResponse>(
        "/auth/login",
        {
            method: "POST",
            body: JSON.stringify(payload),
        },
        {getErrorMessage},
    );
}

export function verifyEmail(token: string) {
    return apiFetch<AuthResponse>(
        `/auth/verify-email?token=${encodeURIComponent(token)}`,
        {
            method: "GET",
        },
        { getErrorMessage },
    );
}


export function resendVerification(payload: ResendVerificationRequest) {
    return apiFetch<RegistrationResponse>(
        "/auth/resend-verification",
        {
            method: "POST",
            body: JSON.stringify(payload),
        },
        { getErrorMessage },
    );
}