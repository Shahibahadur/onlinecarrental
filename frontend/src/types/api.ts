import { User } from './auth';

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

export interface BookingRequest {
  carId: string;
  startDate: string;
  endDate: string;
  pickupLocation: string;
  dropoffLocation: string;
}

export interface SearchFilters {
  location: string;
  pickupLocation: string;
  dropoffLocation: string;
  startDate: string;
  endDate: string;
  carType: string;
  minPrice: number;
  maxPrice: number;
  transmission: string;
  fuelType: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}
