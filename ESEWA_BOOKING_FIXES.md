# ESEWA Integration & Booking Fixes

## Summary of Changes

This document outlines all fixes and improvements made to the ESEWA payment integration and booking system to ensure a smooth payment flow.

---

## 1. Booking Request Validation Fix

### Issue
- `@Future` validation constraint was being applied to `String` date fields
- Hibernate Validator only supports `@Future` on temporal types (LocalDate, LocalDateTime, etc.)
- Error: `HV000030: No validator could be found for constraint 'jakarta.validation.constraints.Future' validating type 'java.lang.String'`

### Fix
**File:** `backend/src/main/java/com/driverental/onlinecarrental/model/dto/request/BookingRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull
    private Long vehicleId;

    @NotNull
    private String startDate;  // Date validation handled in BookingService

    @NotNull
    private String endDate;    // Date validation handled in BookingService

    @NotNull
    private String pickupLocation;

    @NotNull
    private String dropoffLocation;
}
```

**Why This Works:**
- Date validation still occurs in `BookingServiceImpl.createBooking()`
- Frontend validates dates with HTML5 date input min/max attributes
- Backend validates date logic (not past, not inverted)

---

## 2. Duplicate Payment Entry Error Fix

### Issue
- SQL Error: "Duplicate entry '1' for key 'payments.UK_nuscjm6x127hkb15kcb8n56wo'"
- Payment entity has `@OneToOne` unique constraint on `booking_id`
- Calling `initiate()` multiple times created duplicate payment records

### Root Cause
- Users clicking "Pay" multiple times or retrying payment
- Each attempt tried to insert a new payment record instead of updating existing one

### Fix
**File:** `backend/src/main/java/com/driverental/onlinecarrental/service/impl/EsewaPaymentServiceImpl.java`

Added idempotent payment initiation logic:

```java
@Override
@Transactional
public Map<String, String> initiate(EsewaInitiateRequest request) {
    // ... fetch booking and amount ...
    
    // Check if payment already exists for this booking
    if (booking != null) {
        Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
        
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            
            // If payment already completed, reject new attempt
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                throw new BusinessException(
                    "Payment already completed for this booking");
            }
            
            // For PROCESSING/PENDING: reuse transaction UUID and update record
            String uuid = payment.getTransactionId() != null 
                ? payment.getTransactionId() 
                : UUID.randomUUID().toString();
            
            payment.setAmount(amount);
            payment.setPaymentMethod(PaymentMethod.ESEWA);
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setTransactionId(uuid);
            paymentRepository.save(payment);
            
            return buildEsewaFormParams(uuid, amount);
        }
    }
    
    // Create new payment record if none exists
    String uuid = UUID.randomUUID().toString();
    // ... create and save payment ...
    return buildEsewaFormParams(uuid, amount);
}
```

**Benefits:**
- ✅ Users can safely retry payment without duplicating records
- ✅ Reuses same transaction UUID for retry attempts
- ✅ Idempotent operation - safe to call multiple times

---

## 3. Vehicle Availability Check Enhancement

### Issue
- Error: "Vehicle not available for selected dates"
- Could fail even when user's own PENDING booking existed
- Poor error messages didn't help users troubleshoot

### Fix
**File:** `backend/src/main/java/com/driverental/onlinecarrental/service/impl/BookingServiceImpl.java`

#### 3.1 Early Date Validation with Error Messages

```java
@Override
@Transactional
public BookingResponse createBooking(BookingRequest request, Long userId) {
    // ... validate user and vehicle ...
    
    // Parse and validate dates early with try-catch
    LocalDate startDate;
    LocalDate endDate;
    try {
        startDate = LocalDate.parse(request.getStartDate());
        endDate = LocalDate.parse(request.getEndDate());
    } catch (Exception e) {
        log.error("Invalid date format. Start: {}, End: {}", 
            request.getStartDate(), request.getEndDate());
        throw new BusinessException(
            "Invalid date format. Please use YYYY-MM-DD");
    }
    
    // Validate date range
    if (startDate.isAfter(endDate)) {
        throw new BusinessException("Start date cannot be after end date");
    }
    
    if (startDate.isBefore(LocalDate.now())) {
        throw new BusinessException("Start date cannot be in the past");
    }
    
    // Check availability with detailed logging
    if (!isVehicleAvailable(request.getVehicleId(), 
                           request.getStartDate(), 
                           request.getEndDate(), userId)) {
        log.warn("Vehicle {} not available for dates {} to {} (user: {})", 
            request.getVehicleId(), startDate, endDate, userId);
        throw new BusinessException(
            "Vehicle not available for selected dates. Please choose different dates.");
    }
    
    // ... rest of booking creation ...
}
```

#### 3.2 Smart Availability Check (Allows User Retry)

```java
public boolean isVehicleAvailable(Long vehicleId, String startDate, String endDate, Long userId) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);

    List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
        vehicleId, start, end);

    // Filter out user's own PENDING bookings (allow retry)
    List<Booking> otherConflicts = conflictingBookings.stream()
        .filter(b -> !b.getUser().getId().equals(userId) || 
                     !b.getStatus().equals(BookingStatus.PENDING))
        .collect(Collectors.toList());

    if (!otherConflicts.isEmpty()) {
        log.warn("Vehicle {} not available for user {} on dates {} to {}: {} conflicts", 
            vehicleId, userId, start, end, otherConflicts.size());
    } else if (!conflictingBookings.isEmpty()) {
        log.info("User {} is retrying their own PENDING booking for vehicle {} on dates {} to {}", 
            userId, vehicleId, start, end);
    }
    
    return otherConflicts.isEmpty();
}
```

**Key Features:**
- ✅ Distinguishes between user's own bookings and other conflicts
- ✅ Blocks conflicts from OTHER users/bookings
- ✅ Allows retry of user's own pending bookings
- ✅ Detailed logging for debugging
- ✅ Clear error messages to end user

---

## 4. Payment Flow Improvements

### Added Error Handling for:
1. **BusinessException** import in EsewaPaymentServiceImpl
2. **Optional** import for proper Java stream operations
3. **Collectors** import for stream operations
4. Proper `@Transactional` handling for idempotent operations

---

## Testing Scenarios

### Scenario 1: Happy Path
```
User creates booking → ✅ Success
User selects ESEWA → ✅ Payment initiated
User clicks "Pay with eSewa" → ✅ Form submitted
eSewa processes payment → ✅ Success
User gets redirected → ✅ Payment verified
Booking confirmed → ✅ Complete
```

### Scenario 2: Duplicate Payment Attempt
```
User initiates payment (first time) → ✅ Success
User clicks "Pay" again (accidental) → ✅ Reuses same UUID (no duplicate)
eSewa processes → ✅ Duplicate payment detected and rejected
User gets error → ✅ Clear message
```

### Scenario 3: Vehicle No Longer Available
```
User books vehicle for dates → ✅ Success
Another user books same vehicle for overlapping dates → ✅ Accepted
First user tries to confirm → ✅ Error: "Vehicle no longer available"
User gets error message → ✅ Advised to choose different dates
```

### Scenario 4: User Retrying Own Booking
```
User creates booking → ✅ Status: PENDING
Connection drops → ❌ User sees error
User clicks "Confirm Booking" again → ✅ Allowed (not blocked by own booking)
User retries payment → ✅ Reuses transaction UUID
Payment succeeds → ✅ Booking confirmed
```

---

## Configuration

### Environment Variables (if needed)
```bash
# ESEWA Configuration
ESEWA_PRODUCT_CODE=EPAYTEST
ESEWA_SECRET_KEY=8gBm/:&EnhH.1/q
ESEWA_SUCCESS_URL=http://localhost:3000/payment/esewa-success
ESEWA_FAILURE_URL=http://localhost:3000/payment/esewa-failure
```

### application.yaml
```yaml
app:
  esewa:
    product-code: ${ESEWA_PRODUCT_CODE:EPAYTEST}
    secret-key: ${ESEWA_SECRET_KEY:8gBm/:&EnhH.1/q}
    form-url: https://rc-epay.esewa.com.np/api/epay/main/v2/form
    status-url: https://rc.esewa.com.np/api/epay/transaction/status
    success-url: ${ESEWA_SUCCESS_URL:http://localhost:3000/payment/esewa-success}
    failure-url: ${ESEWA_FAILURE_URL:http://localhost:3000/payment/esewa-failure}
```

---

## Files Modified

1. ✅ `BookingRequest.java` - Removed @Future from String fields
2. ✅ `BookingServiceImpl.java` - Enhanced availability check and error handling
3. ✅ `EsewaPaymentServiceImpl.java` - Made payment initiation idempotent
4. ✅ `application.yaml` - Added configuration comments

---

## Debugging Tips

### To see detailed logs:
```yaml
logging:
  level:
    com.driverental.onlinecarrental.service.impl.BookingServiceImpl: DEBUG
    com.driverental.onlinecarrental.service.impl.EsewaPaymentServiceImpl: DEBUG
```

### Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Vehicle not available for selected dates" | Another user booked vehicle for those dates | Choose different dates |
| "Start date cannot be in the past" | Selected date is today or earlier | Choose future date |
| "Invalid date format" | Date not in YYYY-MM-DD format | Check frontend date input |
| "Payment already completed for this booking" | Attempting to pay for already paid booking | Create new booking if needed |

---

## Deployment Checklist

- [ ] Pull latest code with all fixes
- [ ] Run database migrations if any schema changes needed
- [ ] Update environment variables for ESEWA configuration
- [ ] Test booking creation with available vehicle
- [ ] Test ESEWA payment initiation
- [ ] Test payment retry (duplicate attempt)
- [ ] Test payment verification and booking confirmation
- [ ] Monitor logs for any errors
- [ ] Test with unavailable vehicle to verify error message

---

## References

- [ESEWA_INTEGRATION_GUIDE.md](./ESEWA_INTEGRATION_GUIDE.md) - Full ESEWA integration documentation
- [KHALTI_INTEGRATION_GUIDE.md](./KHALTI_INTEGRATION_GUIDE.md) - Khalti integration documentation
