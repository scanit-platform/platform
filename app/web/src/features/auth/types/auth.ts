export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
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
