import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { clearAllAuthStorage } from '../utils/authStorage';

const API_BASE_URL = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
const API_URL = `${API_BASE_URL}/api`;

console.log('API Base URL:', API_URL);

const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Add token to requests
axiosInstance.interceptors.request.use((config) => {
  // Clear any stale tokens before auth requests
  if (config.url?.includes('/auth/')) {
    localStorage.removeItem('authToken');
  }
  
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  // Log request for debugging
  console.log('API Request:', {
    method: config.method?.toUpperCase(),
    url: config.url,
    fullURL: `${config.baseURL}${config.url}`,
    data: config.data,
  });
  
  return config;
});

  // Handle response errors
  axiosInstance.interceptors.response.use(
    (response) => {
      console.log('API Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data,
      });
      return response;
    },
    (error) => {
      // Log error for debugging
      console.error('API Error:', {
        message: error.message,
        code: error.code,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        config: {
          url: error.config?.url,
          fullURL: `${error.config?.baseURL}${error.config?.url}`,
          method: error.config?.method,
          baseURL: error.config?.baseURL,
        }
      });
      
      // Clear expired JWT token
      const errorMessage = error.response?.data?.message || error.message || '';
      if (errorMessage.includes('JWT expired') || errorMessage.includes('Expired')) {
        clearAllAuthStorage();
        window.location.href = '/login';
        return Promise.reject(error);
      }
      
      // Don't redirect on 401 for auth endpoints (login/register)
      // Optional endpoints may return 401 (e.g. personalized recs) — let the page fall back without forcing login
      const url = error.config?.url ?? '';
      const isOptionalAuthFailure =
        url.includes('/recommendations/personalized') || url.includes('/recommendations/hybrid');

      if (
        error.response?.status === 401 &&
        !url.includes('/auth/') &&
        !isOptionalAuthFailure
      ) {
        clearAllAuthStorage();
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

export default axiosInstance;
