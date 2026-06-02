export type AuthMode = "signup" | "signin";

export type AuthField = "name" | "email" | "password";

export type AuthActionState = {
  message: string;
  fieldErrors: Partial<Record<AuthField, string>>;
};

export const initialAuthState: AuthActionState = {
  message: "",
  fieldErrors: {},
};
