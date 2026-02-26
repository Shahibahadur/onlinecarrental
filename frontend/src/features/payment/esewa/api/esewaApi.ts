import axiosInstance from '../../../../api/axios';

export interface EsewaInitiateRequest {
    bookingId?: number;
    amount: number;
}

export type EsewaInitiateResponse = Record<string, string>;

export const esewaAPI = {
    initiate: (data: EsewaInitiateRequest) =>
        axiosInstance.post<EsewaInitiateResponse>('/payment/esewa/initiate', data),

    verify: (uuid: string, amount: string) =>
        axiosInstance.get<{ status: string }>(`/payment/esewa/verify`, {
            params: { uuid, amount },
        }),
};
