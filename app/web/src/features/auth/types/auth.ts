export type RegisterRequest = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
};

export type RegistrationResponse = {
  email: string;
  status: string;
  message: string;
};

export type ResendVerificationRequest = {
  email: string;
};

export type AuthRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  id?: number;
  name?: string;
  email?: string;
  token?: string;
  expiresAt?: string;
};