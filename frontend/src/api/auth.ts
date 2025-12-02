import axiosInstance from './axios';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export const authAPI = {
  login: (data: LoginRequest) =>
    axiosInstance.post<AuthResponse>('/auth/login', data),
  
  register: (data: RegisterRequest) =>
    axiosInstance.post<AuthResponse>('/auth/register', data),
  
  logout: () =>
    axiosInstance.post('/auth/logout'),
  
  getCurrentUser: () =>
    axiosInstance.get('/auth/me'),
  
  refreshToken: () =>
    axiosInstance.post('/auth/refresh'),
};
