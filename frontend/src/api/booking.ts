import axiosInstance from './axios';
import { Booking, BookingRequest } from '../types/api';

export const bookingAPI = {
  getAll: () =>
    axiosInstance.get<Booking[]>('/bookings'),
  
  getById: (id: string) =>
    axiosInstance.get<Booking>(`/bookings/${id}`),
  
  create: (data: BookingRequest) =>
    axiosInstance.post<Booking>('/bookings', data),
  
  update: (id: string, data: Partial<Booking>) =>
    axiosInstance.put<Booking>(`/bookings/${id}`, data),
  
  cancel: (id: string) =>
    axiosInstance.post(`/bookings/${id}/cancel`),
  
  getByUser: () =>
    axiosInstance.get<Booking[]>('/bookings/user/my-bookings'),
};
