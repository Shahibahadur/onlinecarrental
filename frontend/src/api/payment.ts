import axiosInstance from './axios';

export interface EsewaInitiateRequest {
    bookingId?: number;
    amount: number;
}

export type EsewaInitiateResponse = Record<string, string>;

export interface PaymentRequest {
    bookingId: number;
    paymentMethod: 'KHALTI' | 'ESEWA';
}

export interface Payment {
    id: number;
    bookingId: number;
    amount: number;
    paymentMethod: 'KHALTI' | 'ESEWA';
    status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
    transactionId?: string;
    createdAt: string;
    completedAt?: string;
}

export const paymentAPI = {
    create: (data: PaymentRequest) =>
        axiosInstance.post<Payment>('/payments', data),

    getByBooking: (bookingId: number) =>
        axiosInstance.get<Payment>(`/payments/booking/${bookingId}`),

    esewaInitiate: (data: EsewaInitiateRequest) =>
        axiosInstance.post<EsewaInitiateResponse>('/payment/esewa/initiate', data),

    esewaVerify: (uuid: string, amount: string) =>
        axiosInstance.get<{ status: string }>(`/payment/esewa/verify`, {
            params: { uuid, amount },
        }),
};
