import axiosInstance from '../../../../api/axios';

export interface KhaltiInitiateRequest {
    amount: number;
    purchase_order_id: string;
    purchase_order_name: string;
}

export interface KhaltiInitiateResponse {
    pidx: string;
    payment_url: string;
    amount: number;
    expires_at: number;
}

export const khaltiAPI = {
    initiate: (data: KhaltiInitiateRequest) =>
        axiosInstance.post<KhaltiInitiateResponse>('/payment/khalti-initiate', data),
};
