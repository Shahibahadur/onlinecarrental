# Frontend Architecture Overview

## Directory Tree

```
frontend/src/
│
├── 📁 api/                          # API Service Layer
│   ├── axios.ts                    # HTTP Client Configuration
│   ├── auth.ts                     # Authentication Endpoints
│   ├── cars.ts                     # Vehicle Endpoints
│   ├── booking.ts                  # Booking Endpoints
│   ├── recommendations.ts          # Recommendation Endpoints
│   └── index.ts                    # API Exports
│
├── 📁 components/                   # Reusable React Components
│   ├── 📁 Auth/                    # Authentication Components
│   │   ├── LoginForm.tsx
│   │   └── RegisterForm.tsx
│   │
│   ├── 📁 Booking/                 # Booking Components
│   │   ├── BookingFlow.tsx
│   │   ├── BookingModal.tsx
│   │   └── CheckoutForm.tsx
│   │
│   ├── 📁 Car/                     # Car/Vehicle Components
│   │   ├── CarCard.tsx
│   │   ├── CarDetail.tsx
│   │   ├── CarDetailModal.tsx
│   │   ├── CarForm.tsx
│   │   └── CarList.tsx
│   │
│   ├── 📁 Common/                  # Shared/Common Components
│   │   ├── LoadingSpinner.tsx
│   │   ├── SkeletonLoader.tsx
│   │   └── Toast.tsx
│   │
│   ├── 📁 Layout/                  # Layout Components
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   ├── Layout.tsx
│   │   └── ProtectedRoute.tsx
│   │
│   ├── 📁 Recommendation/          # Recommendation Components
│   │   └── RecommendationWidget.tsx
│   │
│   └── 📁 Search/                  # Search Components
│       ├── SearchBar.tsx
│       ├── SearchFilters.tsx
│       └── EnhancedSearchForm.tsx
│
├── 📁 hooks/                        # Custom React Hooks
│   ├── useAuth.ts                  # Authentication Hook
│   ├── useCars.ts                  # Cars Data Hook
│   └── useDebounce.ts              # Debounce Hook
│
├── 📁 pages/                        # Page Components
│   ├── 📁 Admin/                   # Admin Pages
│   │   ├── AdminDashboard.tsx
│   │   ├── BookingManagement.tsx
│   │   └── UserManagement.tsx
│   │
│   ├── 📁 Auth/                    # Authentication Pages
│   │   ├── Login.tsx
│   │   └── Register.tsx
│   │
│   ├── 📁 Car/                     # Car Pages
│   │   ├── CarDetailPage.tsx
│   │   └── SearchResults.tsx
│   │
│   ├── 📁 User/                    # User Pages
│   │   └── Dashboard.tsx
│   │
│   ├── Home.tsx                    # Landing Page
│   ├── NotFound.tsx                # 404 Page
│   └── [Legacy Pages]              # Old pages (deprecated)
│
├── 📁 store/                        # Redux Store
│   ├── auth.store.ts               # Auth Slice
│   ├── booking.store.ts            # Booking Slice
│   ├── search.store.ts             # Search Slice
│   ├── index.ts                    # Store Configuration
│   └── 📁 slices/                  # Legacy Slices (deprecated)
│
├── 📁 types/                        # TypeScript Types
│   ├── api.ts                      # API Types
│   ├── auth.ts                     # Auth Types
│   ├── car.ts                      # Car Types
│   └── index.ts                    # Type Exports
│
├── 📁 utils/                        # Utility Functions
│   ├── constants.ts                # App Constants
│   ├── formatters.ts               # Formatters
│   └── validators.ts               # Validators
│
├── 📁 constants/                    # Constants
│   ├── index.ts
│   ├── locale.ts
│   └── mockData.ts
│
├── 📁 assets/                       # Static Assets
│
├── App.tsx                         # Main App Component
├── main.tsx                        # Entry Point
├── App.css                         # Global Styles
└── index.css                       # Base Styles
```

## Data Flow Architecture

```
┌─────────────────────────────────────────────────────┐
│                    React Components                   │
│              (pages/, components/)                    │
└─────────────────┬───────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────┐
│                  Custom Hooks                        │
│          (useAuth, useCars, useDebounce)             │
└─────────────────┬───────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        ▼                   ▼
┌─────────────────┐  ┌─────────────────────┐
│  Redux Store    │  │   API Service Layer │
│  (auth, etc.)   │  │   (API endpoints)   │
└────────┬────────┘  └──────────┬──────────┘
         │                      │
         └──────────┬───────────┘
                    ▼
        ┌─────────────────────────┐
        │   Axios HTTP Client     │
        │  (with interceptors)    │
        └─────────────────────────┘
                    │
                    ▼
        ┌─────────────────────────┐
        │   Backend API Server    │
        │   (http://localhost:8080│
        └─────────────────────────┘
```

## Module Responsibilities

### API Layer (`/src/api/`)
- Centralized HTTP client configuration
- Request/response interceptors
- Token management
- Error handling
- API endpoint definitions

### Components (`/src/components/`)
- Reusable UI components
- Presentation logic only
- No business logic
- Accept props and callbacks
- Organized by feature/domain

### Hooks (`/src/hooks/`)
- Custom React hooks
- Component logic
- State management integration
- API integration
- Reusable across components

### Pages (`/src/pages/`)
- Full page components
- Compose multiple components
- Handle routing logic
- Page-specific state

### Store (`/src/store/`)
- Global state management
- Redux slices
- Reducers and actions
- Application state

### Types (`/src/types/`)
- TypeScript interfaces
- Type definitions
- API response types
- Component prop types

### Utils (`/src/utils/`)
- Helper functions
- Formatters
- Validators
- Constants

## File Naming Conventions

- **Components**: PascalCase (e.g., `LoginForm.tsx`)
- **Hooks**: camelCase with `use` prefix (e.g., `useAuth.ts`)
- **Utils**: camelCase (e.g., `formatters.ts`)
- **Stores**: name.store.ts (e.g., `auth.store.ts`)
- **Types**: domain-based naming (e.g., `auth.ts`, `car.ts`)

## Dependencies Flow

```
pages/ → components/ ← store/
  ↓         ↓           ↓
hooks/ ←─── ├───────────┤
  ↓                     ↓
api/   ←─── utils/     types/
  ↓
axios (HTTP Client)
```

## Key Integration Points

1. **Components** use **Hooks** for state management
2. **Hooks** integrate with **Store** and **API**
3. **API** uses **Axios** for HTTP requests
4. **Pages** compose **Components** into full pages
5. **Utils** provide helper functions used everywhere
6. **Types** are shared across all modules
