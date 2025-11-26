export interface Car {
  id: string;
  name: string;
  model: string;
  brand: string;
  type: 'SUV' | 'Sedan' | 'Hatchback' | 'Luxury' | 'Sports' | 'Electric';
  pricePerDay: number;
  location: string;
  transmission: 'Automatic' | 'Manual';
  fuelType: 'Petrol' | 'Diesel' | 'Electric' | 'Hybrid';
  seats: number;
  luggage: number;
  image: string;
  images: string[];
  features: string[];
  available: boolean;
  rating: number;
  reviews: number;
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  avatar?: string;
}

export interface Booking {
  id: string;
  carId: string;
  userId: string;
  startDate: string;
  endDate: string;
  totalPrice: number;
  status: 'pending' | 'confirmed' | 'completed' | 'cancelled';
  pickupLocation: string;
  dropoffLocation: string;
  createdAt: string;
}

export interface SearchFilters {
  location: string;
  startDate: string;
  endDate: string;
  carType: string;
  minPrice: number;
  maxPrice: number;
  transmission: string;
  fuelType: string;
}

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
}