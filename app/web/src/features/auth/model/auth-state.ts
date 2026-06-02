export type AuthMode = "signup" | "signin";

export type AuthField =
  | "firstName"
  | "lastName"
  | "email"
  | "phone"
  | "password";

export type AuthActionState = {
  message: string;
  fieldErrors: Partial<Record<AuthField, string>>;
};

export const initialAuthState: AuthActionState = {
  message: "",
  fieldErrors: {},
};
