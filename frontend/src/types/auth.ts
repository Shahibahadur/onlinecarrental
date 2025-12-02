export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  avatar?: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
