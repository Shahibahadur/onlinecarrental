import axiosInstance from './axios';
import { Car, CarFilters } from '../types/car';

export const carsAPI = {
  getAll: (filters?: CarFilters) =>
    axiosInstance.get<Car[]>('/vehicles', { params: filters }),
  
  getById: (id: string) =>
    axiosInstance.get<Car>(`/vehicles/${id}`),
  
  create: (data: Omit<Car, 'id'>) =>
    axiosInstance.post<Car>('/vehicles', data),
  
  update: (id: string, data: Partial<Car>) =>
    axiosInstance.put<Car>(`/vehicles/${id}`, data),
  
  delete: (id: string) =>
    axiosInstance.delete(`/vehicles/${id}`),
  
  search: (query: string) =>
    axiosInstance.get<Car[]>('/vehicles/search', { params: { q: query } }),
};
