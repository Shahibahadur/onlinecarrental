# Project Structure Guide

This document describes the folder structure of the **Online Car Rental** project to help new developers quickly understand the codebase.

---

## Root Directory

```
/
├── database                          # Database setup script (SQL)
├── README.md                         # Project overview
├── PROJECT_STRUCTURE.md              # This file
├── setuptorunprojectoncoddespace     # Codespace setup instructions
├── frontend/                         # React + TypeScript frontend (Vite)
└── onlinecarrental/                  # Spring Boot backend (Java)
```

---

## Backend (`onlinecarrental/`)

Spring Boot application with a layered + feature-based architecture.

```
onlinecarrental/
├── pom.xml                           # Maven dependencies
├── src/main/java/com/driverental/onlinecarrental/
│   ├── OnlineCarRentalApplication.java   # Main entry point
│   │
│   ├── algorithm/                    # Business algorithms
│   │   ├── aho_corasick/             # Text search (Aho-Corasick)
│   │   ├── pricing/                  # Dynamic pricing engine
│   │   └── recommendation/           # Recommendation engine (collaborative filtering)
│   │
│   ├── config/                       # App-wide configuration
│   │   ├── CorsConfig.java
│   │   ├── OpenApiConfig.java
│   │   ├── RedisConfig.java
│   │   ├── SecurityConfig.java
│   │   └── StorageInitializer.java
│   │
│   ├── controller/                   # REST controllers (non-payment)
│   │   ├── AdminController.java
│   │   ├── AdminImageCategoryController.java
│   │   ├── AdminVehicleImageController.java
│   │   ├── AuthController.java
│   │   ├── BookingController.java
│   │   ├── CarsController.java
│   │   ├── ImageController.java
│   │   ├── RecommendationController.java
│   │   ├── ReviewController.java
│   │   ├── UserController.java
│   │   ├── VehicleController.java
│   │   └── VehicleImageController.java
│   │
│   ├── model/                        # Domain models
│   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── request/              # Request DTOs
│   │   │   └── response/             # Response DTOs
│   │   ├── entity/                   # JPA entities
│   │   ├── enums/                    # Enumerations
│   │   └── exception/                # Custom exceptions & global handler
│   │
│   ├── payment/                      # ★ PAYMENT FEATURE (grouped by provider)
│   │   ├── common/                   # Shared payment logic
│   │   │   ├── PaymentController.java
│   │   │   ├── PaymentService.java
│   │   │   ├── PaymentServiceImpl.java
│   │   │   └── PaymentCreationService.java
│   │   ├── esewa/                    # eSewa payment gateway
│   │   │   ├── EsewaPaymentController.java
│   │   │   ├── EsewaPaymentService.java
│   │   │   ├── EsewaPaymentServiceImpl.java
│   │   │   ├── EsewaProperties.java  # eSewa config properties
│   │   │   └── EsewaInitiateRequest.java
│   │   └── khalti/                   # Khalti payment gateway
│   │       ├── KhaltiController.java
│   │       ├── KhaltiService.java
│   │       ├── KhaltiServiceImpl.java
│   │       ├── KhaltiConfig.java     # Khalti config properties
│   │       ├── KhaltiRequest.java
│   │       ├── KhaltiResponse.java
│   │       └── KhaltiCallbackDTO.java
│   │
│   ├── repository/                   # Spring Data JPA repositories
│   │
│   ├── schedular/                    # Scheduled tasks
│   │   └── BookingScheduler.java
│   │
│   ├── security/                     # JWT auth & security
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtService.java
│   │   └── UserPrincipal.java
│   │
│   ├── service/                      # Business service interfaces (non-payment)
│   │   ├── impl/                     # Service implementations
│   │   └── ...Service.java           # Interfaces
│   │
│   └── util/                         # Utility classes
│
├── src/main/resources/
│   └── application.yaml              # Application configuration
│
├── src/test/                         # Unit & integration tests
└── uploads/                          # Uploaded vehicle images
```

### Key Design Decisions (Backend)

- **Payment providers are self-contained**: Each payment gateway (Khalti, eSewa) has its own package under `payment/` containing its controller, service, DTOs, and config. This makes it easy to add new payment providers.
- **Shared payment logic** lives in `payment/common/` (e.g., `PaymentCreationService` for race-condition-safe payment creation).
- **Non-payment features** follow the traditional layered architecture (`controller/`, `service/`, `repository/`).

---

## Frontend (`frontend/`)

React + TypeScript application built with Vite.

```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── index.html
│
└── src/
    ├── App.tsx                       # Root component with routes
    ├── main.tsx                      # Entry point
    │
    ├── api/                          # API client layer (axios-based)
    │   ├── axios.ts                  # Axios instance with interceptors
    │   ├── index.ts                  # Barrel exports
    │   ├── payment.ts                # Payment API (backward-compat re-export)
    │   ├── auth.ts, booking.ts, cars.ts, admin.ts, review.ts, etc.
    │
    ├── features/                     # ★ FEATURE-BASED MODULES
    │   └── payment/                  # Payment feature
    │       ├── index.ts              # Top-level barrel export
    │       ├── common/               # Shared payment types & API
    │       │   ├── api/paymentApi.ts
    │       │   ├── types/payment.ts
    │       │   └── index.ts
    │       ├── esewa/                # eSewa payment feature
    │       │   ├── api/esewaApi.ts
    │       │   ├── pages/
    │       │   │   ├── EsewaCheckout.tsx
    │       │   │   ├── EsewaSuccess.tsx
    │       │   │   └── EsewaFailure.tsx
    │       │   └── index.ts
    │       └── khalti/               # Khalti payment feature
    │           ├── api/khaltiApi.ts
    │           ├── pages/            # (add Khalti pages here as needed)
    │           └── index.ts
    │
    ├── components/                   # Reusable UI components
    │   ├── Auth/                     # Login/Register forms
    │   ├── Booking/                  # Booking modal & flow
    │   ├── Car/                      # Car cards, details, forms
    │   ├── Common/                   # Loading spinners, toasts
    │   ├── Layout/                   # Header, Footer, Layout, ProtectedRoute
    │   ├── Recommendation/           # Recommendation widget
    │   ├── Registration/             # File upload, user type selector
    │   ├── Review/                   # Review form & list
    │   └── Search/                   # Search bar & filters
    │
    ├── pages/                        # Route-level page components
    │   ├── Admin/                    # Admin dashboard, management pages
    │   ├── Auth/                     # Login, Register pages
    │   ├── Car/                      # Car detail, search results
    │   ├── User/                     # User dashboard, reservations
    │   ├── Home.tsx, About.tsx, Contact.tsx, FAQ.tsx, etc.
    │
    ├── store/                        # Redux store
    │   ├── index.ts
    │   ├── slices/                   # Redux slices (auth, booking, search)
    │   └── *.store.ts                # Store files
    │
    ├── types/                        # TypeScript type definitions
    ├── hooks/                        # Custom React hooks
    ├── constants/                    # App constants & locale
    └── utils/                        # Utility functions
```

### Key Design Decisions (Frontend)

- **Feature-based modules** under `features/` group related API calls, pages, types, and components together. Payment is the first feature organized this way.
- **Barrel exports** (`index.ts`) at each level allow clean imports: `import { EsewaCheckout } from './features/payment/esewa'`
- **Backward compatibility**: `api/payment.ts` re-exports from the new feature modules so existing code continues to work.
- **Pages vs Features**: `pages/` contains route-level components for non-feature pages. Feature-specific pages live inside their feature folder.

---

## Adding a New Payment Provider

1. Create a new folder: `payment/<provider-name>/` in both backend and frontend
2. **Backend**: Add controller, service interface, service impl, config, and DTOs
3. **Frontend**: Add `api/<provider>Api.ts`, `pages/`, and `index.ts` barrel export
4. Register the new payment endpoint in `SecurityConfig.java`
5. Add the new provider option in `BookingModal.tsx`

---

## Quick Start

```bash
# Frontend
cd frontend && npm install && npm run dev

# Backend
cd onlinecarrental && ./mvnw spring-boot:run
```
