# Implementation Plan - Car Rental System Updates

## Overview
This document outlines the implementation plan for updating the backend and frontend based on user requirements, with special focus on algorithm integration.

## Requirements Summary

### Car Listing Page
- Search bar with search button
- Search based on input
- Show only available cars
- Table format (not grid)

### User Stories
- ✓ Search for a car
- ✓ See available cars
- ✓ Reserve a car
- ⚠ Return a car (needs implementation)
- ✓ See reserved cars (dashboard exists)
- ⚠ Provide feedback (backend exists, frontend needs integration)
- ⚠ Make payment (basic flow exists, needs enhancement)

### Admin Page
- ✓ Car listing table
- ✓ Add new car button
- ✓ Delete car
- ✓ View user reservations
- ⚠ View user returns (needs implementation)
- ✓ Accept/reject reservations
- ⚠ Return a car (needs implementation)
- ✓ Delete reservation
- ⚠ View feedback (backend exists, frontend needs integration)

### Algorithm Integration
- ✓ Pricing Algorithm: Integrated in BookingService.createBooking via PricingService
- ⚠ Recommendation Algorithm: Backend exists, frontend needs integration in car listing

## Implementation Tasks

### Backend Tasks

1. **Return Car Functionality**
   - Add `returnCar(Long bookingId)` method to BookingService
   - Update BookingStatus if needed (or use COMPLETED)
   - Mark vehicle as available after return
   - Add endpoint in BookingController

2. **Review Controller Completion**
   - Implement full CRUD operations
   - Connect to existing ReviewService

3. **Vehicle Search Enhancement**
   - Ensure search endpoint returns only available cars
   - Integrate with recommendation algorithm for personalized results

### Frontend Tasks

1. **Car Listing Page (Table Format)**
   - Create CarsTable component with table layout
   - Implement search functionality
   - Filter to show only available cars
   - Integrate with backend API

2. **Return Car Feature**
   - Add return button in user dashboard
   - Create return confirmation modal
   - Connect to backend return endpoint

3. **Feedback/Review Integration**
   - Create review form component
   - Add review display in car detail page
   - Connect to ReviewController

4. **Admin Panel Updates**
   - Add "View Returns" section
   - Add "View Feedback" section
   - Add return car functionality in booking management

5. **Algorithm Integration**
   - Integrate recommendation API in car listing page
   - Show recommended cars section
   - Ensure pricing algorithm is used in booking (already done)

## Files to Create/Modify

### Backend
- `BookingService.java` - Add returnCar method
- `BookingServiceImpl.java` - Implement returnCar
- `BookingController.java` - Add return endpoint
- `ReviewController.java` - Complete implementation
- `AdminController.java` - Add return and feedback viewing endpoints

### Frontend
- `CarsTable.tsx` - New table format car listing page
- `api/review.ts` - Review API client
- `components/Review/ReviewForm.tsx` - Review form component
- `components/Review/ReviewList.tsx` - Review display component
- `pages/User/Dashboard.tsx` - Add return car button
- `pages/Admin/BookingManagement.tsx` - Add return car functionality
- `pages/Admin/FeedbackManagement.tsx` - New feedback viewing page
- `App.tsx` - Update routes

## Algorithm Integration Points

1. **Pricing Algorithm**
   - Location: `BookingServiceImpl.createBooking()` line 68
   - Uses: `pricingService.calculateBookingPrice()`
   - Status: ✓ Already integrated

2. **Recommendation Algorithm**
   - Location: `RecommendationController` and `RecommendationService`
   - Integration Point: Car listing page should call `/api/recommendations/personalized`
   - Status: ⚠ Needs frontend integration

## Next Steps
1. Implement return car functionality (backend + frontend)
2. Complete ReviewController
3. Create table format car listing page
4. Integrate recommendation algorithm in frontend
5. Update admin panel for returns and feedback
6. Add payment integration enhancements
