import axiosInstance from '../../../../api/axios';
import type { Payment, PaymentRequest } from '../types/payment';

export const paymentAPI = {
    create: (data: PaymentRequest) =>
        axiosInstance.post<Payment>('/payments', data),

    getByBooking: (bookingId: number) =>
        axiosInstance.get<Payment>(`/payments/booking/${bookingId}`),
};
