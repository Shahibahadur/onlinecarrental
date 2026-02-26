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
