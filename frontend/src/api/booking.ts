import axiosInstance from './axios';
import type { Booking, BookingRequest } from '../types/api';

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
    axiosInstance.get<Booking[]>('/bookings/my'),

  processPayment: (bookingId: string) =>
    axiosInstance.post<Booking>(`/bookings/${bookingId}/pay`),

  returnCar: (id: string) =>
    axiosInstance.put<Booking>(`/bookings/${id}/return`),
};
