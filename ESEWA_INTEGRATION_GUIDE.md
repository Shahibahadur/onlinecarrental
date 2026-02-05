# eSewa Payment Gateway Integration Guide

## Overview
This guide documents the eSewa payment gateway integration for the Online Car Rental application. This implementation supports the eSewa payment method with proper amount formatting, HMAC-SHA256 signature generation, and payment verification.

---

## Backend Implementation

### 1. Configuration

#### EsewaProperties.java
**Location:** `backend/src/main/java/com/driverental/onlinecarrental/config/EsewaProperties.java`

Configuration class for eSewa payment gateway with properties:
- `productCode` - eSewa product code (test: EPAYTEST)
- `secretKey` - eSewa secret key for signing requests
- `formUrl` - eSewa payment form endpoint
- `statusUrl` - eSewa transaction status verification endpoint
- `successUrl` - Redirect URL after successful payment
- `failureUrl` - Redirect URL after failed payment

#### application.yaml Configuration
**Location:** `backend/src/main/resources/application.yaml`

```yaml
app:
  esewa:
    # Test credentials (for Sandbox)
    product-code: ${ESEWA_PRODUCT_CODE:EPAYTEST}
    secret-key: ${ESEWA_SECRET_KEY:8gBm/:&EnhH.1/q}
    form-url: https://rc-epay.esewa.com.np/api/epay/main/v2/form
    status-url: https://rc.esewa.com.np/api/epay/transaction/status
    success-url: ${ESEWA_SUCCESS_URL:http://localhost:3000/payment/esewa-success}
    failure-url: ${ESEWA_FAILURE_URL:http://localhost:3000/payment/esewa-failure}
```

### 2. DTO Classes

#### EsewaInitiateRequest.java
**Location:** `backend/src/main/java/com/driverental/onlinecarrental/model/dto/request/EsewaInitiateRequest.java`

Payload for initiating eSewa payment:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsewaInitiateRequest {
    private Long bookingId;              // Optional: Booking ID to auto-fetch amount
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;           // Payment amount (used if bookingId is null)
}
```

### 3. Service Layer

#### EsewaPaymentService.java (Interface)
**Location:** `backend/src/main/java/com/driverental/onlinecarrental/service/EsewaPaymentService.java`

Service contract for eSewa operations:

```java
public interface EsewaPaymentService {
    // Initiate payment - returns form parameters for frontend
    Map<String, String> initiate(EsewaInitiateRequest request);
    
    // Verify payment status - called after eSewa redirects back
    Map<String, Object> verify(String uuid, String amount);
}
```

#### EsewaPaymentServiceImpl.java (Implementation)
**Location:** `backend/src/main/java/com/driverental/onlinecarrental/service/impl/EsewaPaymentServiceImpl.java`

**Key Features:**

##### initiate(EsewaInitiateRequest)
1. Accepts either `bookingId` or `amount`
2. If `bookingId` provided, fetches booking details and amount
3. Generates unique `transaction_uuid`
4. Creates Payment record with PROCESSING status
5. Builds eSewa form parameters with proper amount formatting
6. Returns parameters for frontend to POST to eSewa

**Critical Implementation Details:**

**Amount Formatting:**
- Whole numbers formatted as integers: "100" (not "100.00")
- Decimal values formatted as plain strings: "100.50"
- This is required for correct HMAC-SHA256 signature verification

```java
// Proper formatting logic
BigDecimal total = new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP);
String formatted = total.stripTrailingZeros().scale() <= 0
    ? String.valueOf(total.intValue())      // "100" for whole numbers
    : total.toPlainString();                 // "100.50" for decimals
```

**Signature Generation:**
- Uses HMAC-SHA256 algorithm
- Same formatted amount value must be used in both form and signature
- Signed fields: "total_amount,transaction_uuid,product_code"
- Signature message format:
  ```
  total_amount={formatted_amount},transaction_uuid={uuid},product_code={code}
  ```

##### verify(String uuid, String amount)
1. Calls eSewa status endpoint with transaction UUID and amount
2. Parses response status
3. Updates Payment record:
   - COMPLETE/COMPLETED → COMPLETED status
   - Other states → FAILED status
4. Confirms booking if payment successful
5. Returns status and raw response

**eSewa Status Endpoint Response:**
```json
{
  "product_code": "EPAYTEST",
  "total_amount": "100",
  "status": "COMPLETE",
  "transaction_uuid": "uuid-string",
  "ref_id": "esewa-ref-id"
}
```

### 4. Controller

#### EsewaPaymentController.java
**Location:** `backend/src/main/java/com/driverental/onlinecarrental/controller/EsewaPaymentController.java`

**Endpoints:**

##### POST /api/payment/esewa/initiate
Initiates eSewa payment.

**Request:**
```json
{
  "bookingId": 123,
  "amount": 5000.00
}
```

**Response:**
```json
{
  "amount": "5000.00",
  "tax_amount": "0",
  "total_amount": "5000",
  "transaction_uuid": "550e8400-e29b-41d4-a716-446655440000",
  "product_code": "EPAYTEST",
  "product_service_charge": "0",
  "product_delivery_charge": "0",
  "success_url": "http://localhost:3000/payment/esewa-success",
  "failure_url": "http://localhost:3000/payment/esewa-failure",
  "signed_field_names": "total_amount,transaction_uuid,product_code",
  "signature": "base64-encoded-hmac-signature"
}
```

##### GET /api/payment/esewa/verify
Verifies payment after eSewa callback.

**Query Parameters:**
- `uuid` - Transaction UUID
- `amount` - Total amount (must match initiated amount)

**Response:**
```json
{
  "status": "COMPLETE",
  "raw": {
    "status": "COMPLETE",
    "transaction_uuid": "uuid",
    ...other fields...
  }
}
```

---

## Frontend Implementation

### 1. Components

#### EsewaCheckout.tsx
**Location:** `frontend/src/pages/Payment/EsewaCheckout.tsx`

**Functionality:**
1. Accepts `bookingId` from query parameter
2. Calls backend `/api/payment/esewa/initiate` to get form parameters
3. Displays hidden form with eSewa submission
4. User clicks "Pay with eSewa" to redirect to eSewa payment page

**Process Flow:**
```
User selects eSewa
  ↓
Frontend fetches eSewa form params
  ↓
User clicks "Pay with eSewa"
  ↓
Form POSTs to eSewa payment endpoint
  ↓
eSewa processes payment
  ↓
eSewa redirects to success_url with data param
```

#### EsewaSuccess.tsx
**Location:** `frontend/src/pages/Payment/EsewaSuccess.tsx`

**Functionality:**
1. Receives payment data in query parameter `data` (Base64 encoded JSON)
2. Decodes data to extract `transaction_uuid` and `total_amount`
3. Calls backend `/api/payment/esewa/verify` to verify payment
4. Displays payment status to user

**Data Decoding:**
```typescript
const data = new URLSearchParams(location.search).get('data');
const decoded = JSON.parse(atob(data));  // Base64 decode then parse JSON
const { transaction_uuid, total_amount } = decoded;
```

**Status Handling:**
- 'COMPLETE' or 'COMPLETED' → Payment successful
- Any other status → Payment failed

### 2. API Integration

#### payment.ts
**Location:** `frontend/src/api/payment.ts`

```typescript
export interface EsewaInitiateRequest {
    bookingId?: number;
    amount: number;
}

export const paymentAPI = {
    esewaInitiate: (data: EsewaInitiateRequest) =>
        axiosInstance.post<EsewaInitiateResponse>('/payment/esewa/initiate', data),

    esewaVerify: (uuid: string, amount: string) =>
        axiosInstance.get<{ status: string }>(`/payment/esewa/verify`, {
            params: { uuid, amount },
        }),
};
```

---

## Payment Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    User Selects eSewa                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ↓
        ┌──────────────────────────────────────┐
        │  Frontend: GET EsewaCheckout page    │
        │  (bookingId as query param)          │
        └──────────┬───────────────────────────┘
                   │
                   ↓
    ┌──────────────────────────────────────────┐
    │ Backend: POST /api/payment/esewa/initiate│
    │ - Create Payment record (PROCESSING)     │
    │ - Generate transaction_uuid              │
    │ - Calculate HMAC-SHA256 signature        │
    │ - Return form parameters                 │
    └──────────┬───────────────────────────────┘
               │
               ↓
    ┌─────────────────────────────────────┐
    │ Frontend: Submit form to eSewa       │
    │ POST https://rc-epay.esewa.com.np/  │
    │ api/epay/main/v2/form               │
    └──────────┬────────────────────────────┘
               │
               ↓ (User enters payment details on eSewa)
               │
               ↓ (eSewa processes payment)
               │
        ┌──────────────────────────────────────┐
        │ eSewa redirects to success_url with:  │
        │ ?data=base64-encoded-json-response    │
        └──────────┬───────────────────────────┘
                   │
                   ↓
    ┌──────────────────────────────────────────┐
    │ Frontend: EsewaSuccess component         │
    │ - Decode data parameter                  │
    │ - Extract transaction_uuid & total_amount│
    │ - Call backend verify endpoint           │
    └──────────┬───────────────────────────────┘
               │
               ↓
    ┌──────────────────────────────────────────┐
    │ Backend: GET /api/payment/esewa/verify   │
    │ - Query eSewa status endpoint             │
    │ - Update Payment record (COMPLETED/FAILED)│
    │ - Confirm Booking if successful          │
    │ - Return verification result             │
    └──────────┬───────────────────────────────┘
               │
               ↓
    ┌──────────────────────────────────────┐
    │ Frontend: Display success/failure     │
    │ message to user                      │
    └──────────────────────────────────────┘
```

---

## Testing

### 1. Test Credentials (Always Use in Development)
```
Product Code: EPAYTEST
Secret Key: 8gBm/:&EnhH.1/q
```

### 2. Test Payment Card Numbers
These credentials work in the eSewa sandbox environment for testing payments.

### 3. End-to-End Testing Checklist

- [ ] Backend validates amount is properly formatted
- [ ] Backend generates correct HMAC-SHA256 signature
- [ ] Frontend successfully submits form to eSewa
- [ ] eSewa processes payment (sandbox mode)
- [ ] eSewa redirects back with encoded data
- [ ] Frontend decodes Base64 data correctly
- [ ] Frontend calls verify endpoint with uuid and amount
- [ ] Backend queries eSewa status endpoint
- [ ] Payment status correctly updated in database
- [ ] Booking status correctly updated to confirmed
- [ ] Frontend displays success message
- [ ] Test with both successful and failed payments

---

## Troubleshooting

### Issue: Signature Verification Fails
**Causes:**
- Amount formatting mismatch (e.g., "100.00" vs "100")
- Secret key incorrect or changed
- Signature message format incorrect

**Solution:**
- Ensure amount is formatted as integer string for whole numbers
- Verify `ESEWA_SECRET_KEY` environment variable
- Check signature message format: `total_amount={amount},transaction_uuid={uuid},product_code={code}`

### Issue: Payment Status Always Returns UNKNOWN
**Causes:**
- Wrong status URL
- Invalid transaction UUID
- Amount mismatch with initiated payment

**Solution:**
- Verify `status-url` in configuration
- Ensure UUID is the same as generated during initiation
- Pass exact amount that was used in initiation

### Issue: Frontend Receives No Data from eSewa
**Causes:**
- Wrong success URL configured
- CORS issues
- Base64 encoding/decoding error

**Solution:**
- Verify `success-url` in backend configuration matches frontend domain
- Check browser console for CORS errors
- Test Base64 decoding manually: `JSON.parse(atob(dataParam))`

### Issue: Payment Created but Not Verified
**Causes:**
- Verify endpoint not called (network error)
- Amount passed to verify doesn't match initiated amount
- Payment not found in database

**Solution:**
- Check browser network tab for verify request
- Logs should show eSewa API response
- Verify amount format is exact: "5000" not "5000.00"

---

## Security Considerations

1. **Secret Key Management:**
   - Never hardcode in source code
   - Use environment variables: `${ESEWA_SECRET_KEY}`
   - Rotate in production periodically

2. **HTTPS Only:**
   - Production URLs must use HTTPS
   - Ensure redirect URLs are HTTPS

3. **Amount Validation:**
   - Validate amount on backend (don't trust frontend)
   - Verify returned amount matches initiated amount
   - Check for negative or zero amounts

4. **Transaction UUID:**
   - Generate server-side (not from user input)
   - UUID should be cryptographically random
   - Store and validate on verification

5. **Payment Verification:**
   - Always verify with eSewa on successful redirect
   - Don't trust client-side payment status
   - Update database atomically to prevent duplicates

---

## Environment Variables

Required for production deployment:

```bash
# eSewa Configuration
export ESEWA_PRODUCT_CODE=YOUR_PRODUCT_CODE
export ESEWA_SECRET_KEY=YOUR_SECRET_KEY
export ESEWA_SUCCESS_URL=https://yourdomain.com/payment/esewa-success
export ESEWA_FAILURE_URL=https://yourdomain.com/payment/esewa-failure
```

---

## API Reference

### POST /api/payment/esewa/initiate
Initiates a new eSewa payment, returns form parameters for submission.

**Request Headers:**
- Content-Type: application/json

**Request Body:**
```json
{
  "bookingId": 123,
  "amount": 5000.00
}
```

**Response (200 OK):**
```json
{
  "amount": "5000",
  "tax_amount": "0",
  "total_amount": "5000",
  "transaction_uuid": "550e8400-e29b-41d4-a716-446655440000",
  "product_code": "EPAYTEST",
  "product_service_charge": "0",
  "product_delivery_charge": "0",
  "success_url": "...",
  "failure_url": "...",
  "signed_field_names": "total_amount,transaction_uuid,product_code",
  "signature": "..."
}
```

### GET /api/payment/esewa/verify
Verifies a completed eSewa payment transaction.

**Query Parameters:**
- `uuid` (string, required) - Transaction UUID from eSewa response
- `amount` (string, required) - Total amount that was paid

**Response (200 OK):**
```json
{
  "status": "COMPLETE",
  "raw": {
    "status": "COMPLETE",
    "transaction_uuid": "...",
    "product_code": "...",
    "total_amount": "5000",
    "ref_id": "..."
  }
}
```

---

## Related Documentation

- [eSewa API Documentation](https://esewa.com.np/)
- [KHALTI_INTEGRATION_GUIDE.md](./KHALTI_INTEGRATION_GUIDE.md)
- Backend Configuration: `backend/src/main/resources/application.yaml`
- Frontend API: `frontend/src/api/payment.ts`

---

## Version History

- **v1.0** (2026-02-05): Initial eSewa integration documentation
  - Fixed amount formatting for proper signature verification
  - Updated configuration with environment variables
  - Added comprehensive troubleshooting guide

---

## Support

For issues or questions:
1. Check the Troubleshooting section
2. Review eSewa API documentation
3. Check application logs: `logs/car-rental-app.log`
4. Review browser console for frontend errors
