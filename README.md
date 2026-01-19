# Car Rental Website Project

This is a car rental website project developed as a 6th semester college project. The project includes user authentication, car listings, reservations, administrative functionalities, and advanced algorithms for pricing and recommendations.

![Car Rental Project](https://img.shields.io/badge/Project-Car%20Rental-blue) ![Status](https://img.shields.io/badge/Status-Completed-success)

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Introduction](#introduction)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Requirements](#requirements)
- [Installation](#installation)
- [Database](#database)
- [Project Structure](#project-structure)
- [Algorithms Implemented](#algorithms-implemented)
- [User Story](#user-story)
- [Admin Panel](#admin-panel)
- [License](#license)
- [Authors](#authors)

## Introduction

This is a comprehensive car rental website project designed for the Nepali market. The purpose of this project is to create a full-stack car rental website with advanced features including:

- User authentication and registration
- Car listing with search and filtering
- Car reservation and booking system
- Admin panel for management
- Dynamic pricing algorithm
- Recommendation system using matrix factorization

The website provides a complete solution for car rental management with modern UI/UX and robust backend functionality.

## Features

### User Features
- User registration and login
- Browse available cars with search and filters
- View detailed car information
- Make reservations with date selection
- View booking history
- Dynamic pricing based on multiple factors

### Admin Features
- **Admin Dashboard** with real-time statistics
  - Total cars, users, bookings, revenue
- **Vehicle Management**
  - Add new vehicles with image upload
  - Edit existing vehicle details
  - Delete vehicles with confirmation
  - Toggle vehicle availability
  - Search and filter vehicles
- **Booking Management**
  - View all reservations with customer details
  - Accept or reject pending bookings
  - Return completed bookings
  - Delete reservations
  - Filter bookings by status
- **Feedback Management**
  - View customer reviews and ratings
  - Delete inappropriate reviews
  - Search feedback
- **User Management**
  - View all registered users
  - Manage user accounts
  - Delete users if needed
- User management
- Vehicle availability management

## Technologies Used

### Frontend
- **React** - UI library
- **TypeScript** - Type-safe JavaScript
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Redux Toolkit** - State management
- **React Query** - Data fetching and caching
- **React Router** - Routing

### Backend
- **Java Spring Boot** - Backend framework
- **Spring Security** - Authentication and authorization
- **JPA/Hibernate** - ORM for database operations
- **MySQL** - Database
- **Maven** - Dependency management

### Algorithms
- **Dynamic Pricing Algorithm** - Multi-factor pricing system
- **Matrix Factorization** - Recommendation system using collaborative filtering

## Requirements

- **Java JDK 17+** - For backend
- **Node.js 18+** - For frontend
- **MySQL 8.0+** - Database
- **Maven 3.8+** - Build tool
- **npm or yarn** - Package manager

## Installation

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd onlinecarrental/backend
   ```

2. Configure database in `src/main/resources/application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/carrental
       username: your_username
       password: your_password
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The backend will be running on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd onlinecarrental/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure API URL in `.env` file (if needed):
   ```env
   VITE_API_URL=http://localhost:8080/api
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will be running on `http://localhost:5173`

### Database Setup

1. Create a MySQL database:
   ```sql
   CREATE DATABASE carrental;
   ```

2. The application will automatically create tables on first run (using JPA auto-ddl)
   - Or import schema manually if configured

## Database

- **Database**: MySQL
- **Tables**: 
  - `users` - User accounts
  - `vehicles` - Car listings
  - `bookings` - Reservation records
  - `reviews` - User reviews (if implemented)

## Project Structure

```
onlinecarrental/
├── backend/
│   ├── src/main/java/com/driverental/onlinecarrental/
│   │   ├── algorithm/
│   │   │   ├── pricing/          # Dynamic pricing algorithm
│   │   │   └── recommendation/   # Matrix factorization algorithm
│   │   ├── controller/           # REST API controllers
│   │   ├── service/              # Business logic
│   │   ├── repository/           # Data access layer
│   │   ├── model/                # Entity and DTO classes
│   │   └── config/               # Configuration classes
│   └── src/main/resources/
│       └── application.yaml      # Application configuration
│
└── frontend/
    ├── src/
    │   ├── pages/                # Page components
    │   ├── components/           # Reusable components
    │   ├── api/                  # API clients
    │   ├── store/                # Redux store
    │   └── types/                # TypeScript types
    └── package.json
```

## Algorithms Implemented

### 1. Dynamic Pricing Algorithm
Located in: `backend/src/main/java/com/driverental/onlinecarrental/algorithm/pricing/`

The pricing algorithm considers multiple factors:
- Base vehicle price
- Demand factors
- Seasonal variations
- Booking duration
- Lead time
- Vehicle type and features
- Location-based pricing

**Key Classes:**
- `DynamicPricingEngine` - Main pricing engine
- `PriceFactorCalculator` - Calculates pricing factors
- `DemandCalculator` - Calculates demand-based adjustments

### 2. Matrix Factorization Algorithm
Located in: `backend/src/main/java/com/driverental/onlinecarrental/algorithm/recommendation/`

The recommendation system uses matrix factorization for collaborative filtering:
- User-item matrix construction
- Latent feature extraction
- Stochastic Gradient Descent (SGD) for training
- Rating prediction
- Top-N recommendations

**Key Classes:**
- `MatrixFactorization` - Core matrix factorization implementation
- `CollaborativeFiltering` - User and item-based filtering
- `HybridRecommender` - Combines multiple recommendation strategies

## User Story

- As a user, I want to register and login to the system
- As a user, I want to search for available cars
- As a user, I want to view detailed car information
- As a user, I want to make a reservation with date selection
- As a user, I want to see my booking history
- As a user, I want to receive personalized car recommendations
- As a user, I want to see dynamic pricing based on various factors

## Admin Panel

The admin panel provides comprehensive management capabilities:

- **Dashboard**: Overview with statistics (total users, vehicles, bookings, revenue)
- **Vehicle Management**: 
  - Add new vehicles
  - Edit vehicle details
  - Delete vehicles
  - Update availability status
- **Booking Management**:
  - View all bookings
  - Update booking status (Pending, Confirmed, Completed, Cancelled)
  - Filter and search bookings
- **User Management**:
  - View all users
  - Delete user accounts
  - View user details

Access admin panel at: `/admin` (requires admin role)

## License

This project is developed as a college project. All rights reserved.

## Authors

- Developed as a 6th semester college project
- Includes advanced algorithms for pricing and recommendations

---

**Note**: This project is for educational purposes. The algorithms (Dynamic Pricing and Matrix Factorization) are mandatory components of this project.
