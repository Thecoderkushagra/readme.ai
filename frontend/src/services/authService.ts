import { apiClient } from './apiClient';
import type { ApiResponse, AuthResponse, LoginRequest, SignupRequest, SignupResponse } from '../types/auth';

export const login = async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
  const response = await apiClient.post('/api/auth/login', data);
  return response.data as ApiResponse<AuthResponse>;
};

export const signup = async (data: SignupRequest): Promise<ApiResponse<SignupResponse>> => {
  const response = await apiClient.post('/api/auth/signup', data);
  return response.data as ApiResponse<SignupResponse>;
};

export const refreshToken = async (token: string): Promise<ApiResponse<AuthResponse>> => {
  const response = await apiClient.post('/api/auth/refresh', {
    refreshToken: token,
  });
  return response.data as ApiResponse<AuthResponse>;
};
