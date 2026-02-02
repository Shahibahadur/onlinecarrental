# Khalti & Esewa Payment Gateway Integration Guide

## Overview
This guide documents the payment gateway integration for the Online Car Rental application. The system now supports **Khalti** and **Esewa** payment methods, with all other payment methods removed.

## Payment Methods
- ✅ **Khalti** - Primary payment gateway
- ✅ **Esewa** - Alternative payment gateway
- ❌ Removed: Credit Card, Debit Card, PayPal, Cash, Bank Transfer

---

## Backend Implementation

### 1. Configuration Files

#### KhaltiConfig.java
**Location:** `src/main/java/com/driverental/onlinecarrental/config/KhaltiConfig.java`

Configuration class for Khalti payment gateway. Reads properties from `application.yaml`.

**Required Properties:**
```yaml
khalti:
  liveSecretKey: YOUR_KHALTI_SECRET_KEY
  livePublicKey: YOUR_KHALTI_PUBLIC_KEY
  initialUrl: https://khalti.com/api/v2/epay/initiate/
  verifyUrl: https://khalti.com/api/v2/epay/complete/
  websiteUrl: http://localhost:8080
  callbackUrl: http://localhost:8080/api/payment/khalti-response-handle
```

### 2. DTO Classes

#### KhaltiRequest.java
**Location:** `src/main/java/com/driverental/onlinecarrental/model/dto/khalti/KhaltiRequest.java`

Request object for initiating Khalti payment.

**Fields:**
- `amount` - Amount in paisa (e.g., Rs 50 = 5000 paisa)
- `purchase_order_id` - Unique order identifier (format: BOOKING_<bookingId>)
- `purchase_order_name` - Order name/description

#### KhaltiResponse.java
**Location:** `src/main/java/com/driverental/onlinecarrental/model/dto/khalti/KhaltiResponse.java`

Response object from Khalti after payment initiation.

**Fields:**
- `pidx` - Payment index from Khalti
- `payment_url` - URL to redirect user for payment
- `amount` - Amount in paisa
- `expires_at` - Payment expiration timestamp

#### KhaltiCallbackDTO.java
**Location:** `src/main/java/com/driverental/onlinecarrental/model/dto/khalti/KhaltiCallbackDTO.java`

Callback data received from Khalti after payment completion.

**Fields:**
- `pidx` - Payment index
- `transaction_id` - Unique transaction ID from Khalti
- `status` - Payment status (COMPLETED, PENDING, etc.)
- `amount` - Amount in paisa
- `purchase_order_id` - Original order ID
- `purchase_order_name` - Order name
- `paymentMethod` - Set to KHALTI by default

### 3. Service Layer

#### KhaltiService.java
**Location:** `src/main/java/com/driverental/onlinecarrental/service/impl/KhaltiService.java`

Main service for Khalti payment operations.

**Methods:**

##### initiatePayment(KhaltiRequest khalti)
Initiates a payment with Khalti.

**Request Example:**
```json
{
  "amount": 500000,
  "purchase_order_id": "BOOKING_123",
  "purchase_order_name": "Car Rental Booking"
}
```

**Response Example:**
```json
{
  "pidx": "GBepCKd8U6J16K",
  "payment_url": "https://khalti.com/checkout/GBepCKd8U6J16K",
  "amount": 500000,
  "expires_at": 1644400000
}
```

**Process:**
1. Creates request body with payment details
2. Sets authorization header with secret key
3. POSTs to Khalti initiate endpoint
4. Returns payment URL for redirect

##### verifyPayment(KhaltiCallbackDTO response)
Verifies payment completion with Khalti API.

**Process:**
1. Checks if payment status is "COMPLETED"
2. Verifies transaction with Khalti using pidx
3. Compares transaction IDs
4. Updates payment status in database
5. Returns true if verified, false otherwise

##### generateUniqueId()
Generates unique transaction UUID.

### 4. Controller

#### KhaltiController.java
**Location:** `src/main/java/com/driverental/onlinecarrental/controller/KhaltiController.java`

REST endpoints for Khalti payment handling.

**Endpoints:**

##### POST /api/payment/khalti-initiate
Initiates Khalti payment.

**Request:**
```json
{
  "amount": 500000,
  "purchase_order_id": "BOOKING_123",
  "purchase_order_name": "Car Rental"
}
```

**Response:**
```json
{
  "pidx": "GBepCKd8U6J16K",
  "payment_url": "https://khalti.com/checkout/GBepCKd8U6J16K",
  "amount": 500000,
  "expires_at": 1644400000
}
```

**Usage:**
```javascript
const response = await fetch('/api/payment/khalti-initiate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: 500000,
    purchase_order_id: 'BOOKING_123',
    purchase_order_name: 'Car Rental'
  })
});
const data = await response.json();
// Redirect user to payment_url
window.location.href = data.payment_url;
```

##### GET /api/payment/khalti-response-handle
Handles Khalti callback after payment.

**Query Parameters:**
- `pidx` - Payment index
- `transaction_id` - Transaction ID
- `status` - Payment status
- `amount` - Amount in paisa
- `purchase_order_id` - Order ID

**Response:**
- Redirects to `/success.html` if payment verified
- Redirects to `/failure.html` if payment failed

### 5. Model Updates

#### PaymentMethod.java
**Location:** `src/main/java/com/driverental/onlinecarrental/model/enums/PaymentMethod.java`

Updated to include only:
```java
public enum PaymentMethod {
    KHALTI,
    ESEWA
}
```

### 6. Security Configuration Updates

#### SecurityConfig.java
**Location:** `src/main/java/com/driverental/onlinecarrental/config/SecurityConfig.java`

**Changes:**
- Added Khalti endpoints to permitAll list: `/api/payment/khalti/**`
- Added RestTemplate bean for HTTP calls to Khalti API

**Security Rules:**
```java
.requestMatchers("/api/payment/esewa/**", "/api/payment/khalti/**").permitAll()
```

### 7. Application Configuration

#### application.yaml
**Location:** `src/main/resources/application.yaml`

**Added Khalti Configuration:**
```yaml
khalti:
  liveSecretKey: YOUR_KHALTI_SECRET_KEY
  livePublicKey: YOUR_KHALTI_PUBLIC_KEY
  initialUrl: https://khalti.com/api/v2/epay/initiate/
  verifyUrl: https://khalti.com/api/v2/epay/complete/
  websiteUrl: http://localhost:8080
  callbackUrl: http://localhost:8080/api/payment/khalti-response-handle
```

---

## Frontend Integration

### Payment Flow

1. **Checkout Page:**
   - Display payment method selection (Khalti / Esewa)
   - Collect booking details

2. **For Khalti:**
```typescript
// 1. Call backend to initiate payment
const response = await fetch('/api/payment/khalti-initiate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    amount: totalAmount * 100, // Convert to paisa
    purchase_order_id: `BOOKING_${bookingId}`,
    purchase_order_name: 'Car Rental Booking'
  })
});

const data = await response.json();

// 2. Redirect to Khalti payment URL
window.location.href = data.payment_url;

// 3. Khalti redirects back to:
// /api/payment/khalti-response-handle?pidx=...&transaction_id=...&status=...
```

3. **Payment Verification:**
   - Backend verifies payment with Khalti
   - Updates payment status in database
   - Redirects to success/failure page

### Frontend Components to Update

Update the payment selection component to display only Khalti and Esewa:

```typescript
const paymentMethods = [
  { id: 'KHALTI', name: 'Khalti', icon: 'khalti-logo' },
  { id: 'ESEWA', name: 'Esewa', icon: 'esewa-logo' }
];
```

---

## Testing

### Prerequisites
1. Create Khalti account at https://khalti.com
2. Get test credentials:
   - Secret Key
   - Public Key
3. Update application.yaml with test credentials

### Test Cases

#### 1. Payment Initiation
```bash
curl -X POST http://localhost:8080/api/payment/khalti-initiate \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000,
    "purchase_order_id": "BOOKING_123",
    "purchase_order_name": "Test Booking"
  }'
```

Expected Response:
```json
{
  "pidx": "GBepCKd8U6J16K",
  "payment_url": "https://khalti.com/checkout/GBepCKd8U6J16K",
  "amount": 500000,
  "expires_at": 1644400000
}
```

#### 2. Payment Callback Simulation
Open browser and navigate to:
```
http://localhost:8080/api/payment/khalti-response-handle?pidx=GBepCKd8U6J16K&transaction_id=TEST123&status=COMPLETED&amount=500000&purchase_order_id=BOOKING_123&purchase_order_name=Test
```

---

## Environment Setup

### Development
```yaml
khalti:
  liveSecretKey: test_secret_key
  livePublicKey: test_public_key
  initialUrl: https://khalti.com/api/v2/epay/initiate/
  verifyUrl: https://khalti.com/api/v2/epay/complete/
  websiteUrl: http://localhost:8080
  callbackUrl: http://localhost:8080/api/payment/khalti-response-handle
```

### Production
Replace with actual production credentials:
```yaml
khalti:
  liveSecretKey: ${KHALTI_SECRET_KEY}
  livePublicKey: ${KHALTI_PUBLIC_KEY}
  initialUrl: https://khalti.com/api/v2/epay/initiate/
  verifyUrl: https://khalti.com/api/v2/epay/complete/
  websiteUrl: https://yourdomain.com
  callbackUrl: https://yourdomain.com/api/payment/khalti-response-handle
```

---

## API Reference

### Payment Initiation
- **Endpoint:** `POST /api/payment/khalti-initiate`
- **Auth:** Not required
- **Body:** `KhaltiRequest`
- **Response:** `KhaltiResponse`

### Payment Callback
- **Endpoint:** `GET /api/payment/khalti-response-handle`
- **Auth:** Not required
- **Params:** Query parameters from Khalti
- **Response:** Redirect to success/failure page

---

## Troubleshooting

### Issue: Payment verification fails
**Solution:** 
- Verify Secret Key is correct
- Check transaction_id format
- Ensure callback URL is accessible

### Issue: 401 Unauthorized from Khalti
**Solution:**
- Verify Authorization header format: `Key {secretKey}`
- Check if credentials are valid

### Issue: Payment amount mismatch
**Solution:**
- Ensure amount is in paisa (multiply by 100)
- Verify amount in callback matches initiated amount

---

## Security Considerations

1. **Never expose Secret Key** - Keep it in environment variables
2. **Verify all callbacks** - Always verify payment status with Khalti API
3. **HTTPS only** - Use HTTPS for all payment-related endpoints in production
4. **Transaction ID verification** - Always compare transaction IDs
5. **Rate limiting** - Implement rate limiting on payment endpoints

---

## Files Modified/Created

### Created Files
- `config/KhaltiConfig.java`
- `model/dto/khalti/KhaltiRequest.java`
- `model/dto/khalti/KhaltiResponse.java`
- `model/dto/khalti/KhaltiCallbackDTO.java`
- `service/impl/KhaltiService.java`
- `controller/KhaltiController.java`

### Modified Files
- `model/enums/PaymentMethod.java`
- `config/SecurityConfig.java`
- `resources/application.yaml`

---

## Next Steps

1. **Add Khalti Public Key to Frontend:**
   - Create payment form component
   - Integrate with Khalti JavaScript SDK

2. **Database Migration:**
   - Run flyway/liquibase migrations to update payment_method enum

3. **Frontend Payment Component:**
   - Create payment selection UI
   - Implement payment flow
   - Add success/failure handling pages

4. **Testing:**
   - Perform end-to-end testing
   - Test with Khalti sandbox
   - Verify payment status updates

5. **Deployment:**
   - Update production configuration
   - Set up monitoring for payment failures
   - Configure email notifications

---

## Support

For Khalti API documentation: https://docs.khalti.com/
For Esewa integration: Check existing Esewa configuration in the project
