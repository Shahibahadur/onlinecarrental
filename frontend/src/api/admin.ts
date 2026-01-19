import axiosInstance from './axios';
import type { Car } from '../types/car';
import type { Booking } from '../types/api';
import type { User } from '../types';

interface VehicleResponse {
  id: number | string;
  make: string;
  model: string;
  type: string;
  dailyPrice: number;
  location: string;
  transmission: string;
  fuelType: string;
  seats: number;
  luggageCapacity: number;
  imageUrl?: string | null;
  isAvailable?: boolean;
  rating?: number;
  reviewCount?: number;
  features?: string[];
}

interface VehicleRequest {
  make: string;
  model: string;
  year: number;
  type: string;
  fuelType: string;
  transmission: string;
  seats: number;
  luggageCapacity: number;
  features?: string[];
  basePrice: number;
  dailyPrice: number;
  location: string;
  imageUrl?: string;
  isAvailable: boolean;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
const backendOrigin = apiBaseUrl;

const normalizeImageUrl = (url?: string | null) => {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) return `${backendOrigin}${url}`;
  return url;
};

const normalizeVehicleType = (type?: string): Car['type'] => {
  const t = String(type || '').toUpperCase();
  if (t === 'SUV') return 'SUV';
  if (t === 'SEDAN') return 'Sedan';
  if (t === 'HATCHBACK') return 'Hatchback';
  if (t === 'LUXURY') return 'Luxury';
  if (t === 'SPORTS') return 'Sports';
  if (t === 'ELECTRIC') return 'Electric';
  if (t === 'HYBRID') return 'Electric';
  return 'SUV';
};

const normalizeFuelType = (fuelType?: string): Car['fuelType'] => {
  const f = String(fuelType || '').toUpperCase();
  if (f === 'PETROL') return 'Petrol';
  if (f === 'DIESEL') return 'Diesel';
  if (f === 'ELECTRIC') return 'Electric';
  if (f === 'HYBRID') return 'Hybrid';
  return 'Petrol';
};

const transformVehicleToCar = (vehicle: VehicleResponse): Car => {
  const image = normalizeImageUrl(vehicle.imageUrl);
  return {
    id: String(vehicle.id),
    name: `${vehicle.make} ${vehicle.model}`,
    brand: vehicle.make,
    model: vehicle.model,
    type: normalizeVehicleType(vehicle.type),
    pricePerDay: vehicle.dailyPrice ? Number(vehicle.dailyPrice) : 0,
    location: vehicle.location,
    transmission: vehicle.transmission as Car['transmission'],
    fuelType: normalizeFuelType(vehicle.fuelType),
    seats: vehicle.seats,
    luggage: vehicle.luggageCapacity,
    image,
    images: image ? [image] : [],
    features: vehicle.features || [],
    available: vehicle.isAvailable ?? true,
    rating: vehicle.rating || 0,
    reviews: vehicle.reviewCount || 0,
  };
};

const toVehicleRequest = (data: Partial<Car> & any): VehicleRequest => {
  const make = data.brand || (typeof data.name === 'string' ? data.name.split(' ')[0] : '');
  const model = data.model || '';
  const year = Number(data.year || new Date().getFullYear());
  const basePrice = Number(data.basePrice ?? data.pricePerDay ?? 0);
  const dailyPrice = Number(data.pricePerDay ?? 0);
  const luggageCapacity = Number(data.luggage ?? data.luggageCapacity ?? 0);

  return {
    make,
    model,
    year,
    type: String(data.type || 'SEDAN').toUpperCase(),
    fuelType: String(data.fuelType || 'PETROL').toUpperCase().replace(/\s+/g, '_'),
    transmission: String(data.transmission || ''),
    seats: Number(data.seats ?? 0),
    luggageCapacity,
    features: Array.isArray(data.features) ? data.features : [],
    basePrice,
    dailyPrice,
    location: String(data.location || ''),
    imageUrl: String(data.image || ''),
    isAvailable: data.available !== false,
  };
};

export interface AdminStats {
  totalUsers: number;
  totalCars: number;
  totalBookings: number;
  revenue: number;
  activeBookings: number;
  pendingBookings?: number;
  completedBookings?: number;
  monthlyRevenue?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
}

export const adminAPI = {
  // Dashboard Statistics
  getStats: () =>
    axiosInstance.get<AdminStats>('/admin/dashboard/stats'),

  // User Management
  getAllUsers: (page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedResponse<User>>('/admin/users', {
      params: { page, size },
    }),

  getUserById: (id: string) =>
    axiosInstance.get<User>(`/users/${id}`),

  updateUser: (id: string, data: Partial<User>) =>
    axiosInstance.put<User>(`/users/${id}`, data),

  deleteUser: (id: string) =>
    axiosInstance.delete(`/users/${id}`),

  // Vehicle Management
  getAllVehicles: (page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedResponse<VehicleResponse>>('/admin/vehicles', {
      params: { page, size },
    }).then((resp) => {
      const data = resp.data;
      const content = Array.isArray(data.content) ? data.content.map(transformVehicleToCar) : [];
      return {
        ...resp,
        data: {
          ...data,
          content,
        },
      } as any;
    }),

  createVehicle: (data: Omit<Car, 'id'>) =>
    axiosInstance.post('/vehicles', toVehicleRequest(data)),

  updateVehicle: (id: string, data: Partial<Car>) =>
    axiosInstance.put(`/vehicles/${id}`, toVehicleRequest(data)),

  uploadVehicleImage: (file: File, category?: string) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosInstance.post<string>('/images/vehicles/upload', formData, {
      params: { category: category || 'general' },
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  deleteVehicle: (id: string) =>
    axiosInstance.delete(`/vehicles/${id}`),

  updateVehicleAvailability: (id: string, available: boolean) =>
    axiosInstance.put<Car>(`/admin/vehicles/${id}/availability`, null, {
      params: { available },
    }),

  // Booking Management
  getAllBookings: (page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedResponse<Booking>>('/admin/bookings', {
      params: { page, size },
    }),

  getBookingById: (id: string) =>
    axiosInstance.get<Booking>(`/bookings/${id}`),

  updateBookingStatus: (id: string, status: string) =>
    axiosInstance.put<Booking>(`/admin/bookings/${id}/status`, null, {
      params: { status },
    }),

  cancelBooking: (id: string) =>
    axiosInstance.post(`/bookings/${id}/cancel`),
  
  returnCar: (id: string) =>
    axiosInstance.put<Booking>(`/admin/bookings/${id}/return`),

  // Review/Feedback Management
  getAllReviews: (page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedResponse<any>>('/admin/reviews', {
      params: { page, size },
    }),

  deleteReview: (id: string) =>
    axiosInstance.delete(`/admin/reviews/${id}`),
};
