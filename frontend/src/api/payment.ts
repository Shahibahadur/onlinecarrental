/**
 * @deprecated - Import directly from feature modules instead:
 *   import { esewaAPI } from '../features/payment/esewa';
 *   import { khaltiAPI } from '../features/payment/khalti';
 *   import { paymentAPI } from '../features/payment/common';
 *
 * This file is kept for backward compatibility.
 */
import { esewaAPI } from '../features/payment/esewa';
import { paymentAPI as commonPaymentAPI } from '../features/payment/common';

export type { EsewaInitiateRequest, EsewaInitiateResponse } from '../features/payment/esewa';
export type { Payment, PaymentRequest } from '../features/payment/common';

export const paymentAPI = {
    ...commonPaymentAPI,
    esewaInitiate: esewaAPI.initiate,
    esewaVerify: esewaAPI.verify,
};
