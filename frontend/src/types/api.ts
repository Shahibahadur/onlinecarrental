export interface Booking {
  id: string | number;
  carId?: string;
  vehicleId?: string | number;
  userId: string | number;
  startDate: string;
  endDate: string;
  totalPrice: number;
  status:
    | 'pending'
    | 'confirmed'
    | 'completed'
    | 'cancelled'
    | 'PENDING'
    | 'CONFIRMED'
    | 'ACTIVE'
    | 'COMPLETED'
    | 'CANCELLED';
  pickupLocation: string;
  dropoffLocation: string;
  createdAt: string;

  vehicle?: {
    id?: string | number;
    make?: string;
    model?: string;
    type?: string;
    location?: string;
    dailyPrice?: number;
    imageUrl?: string;
    isAvailable?: boolean;
  };
}

export interface BookingRequest {
  vehicleId?: string | number;
  carId?: string;
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
