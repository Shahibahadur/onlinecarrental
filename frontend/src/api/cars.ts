import axiosInstance from './axios';
import type { Car, CarFilters } from '../types/car';

// Backend VehicleResponse interface (what we receive from API)
interface VehicleResponse {
  id: number;
  make: string;
  model: string;
  year?: number;
  type: string;
  fuelType: string;
  transmission: string;
  seats: number;
  luggageCapacity: number;
  features: string[];
  basePrice?: number;
  dailyPrice: number;
  location: string;
  imageUrl?: string;
  isAvailable: boolean;
  rating?: number;
  reviewCount?: number;
}

// Transform backend VehicleResponse to frontend Car interface
const transformVehicleToCar = (vehicle: VehicleResponse): Car => {
  const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
  const backendOrigin = apiBaseUrl.replace(/\/api\/?$/, '');

  const normalizeImageUrl = (url?: string | null) => {
    if (!url) return '';
    if (url.startsWith('http://') || url.startsWith('https://')) return url;
    if (url.startsWith('/')) return `${backendOrigin}${url}`;
    return url;
  };

  const imageUrl = normalizeImageUrl(vehicle.imageUrl);

  return {
    id: String(vehicle.id),
    name: `${vehicle.make} ${vehicle.model}`,
    brand: vehicle.make,
    model: vehicle.model,
    type: vehicle.type as Car['type'],
    pricePerDay: vehicle.dailyPrice ? Number(vehicle.dailyPrice) : 0,
    location: vehicle.location,
    transmission: vehicle.transmission as Car['transmission'],
    fuelType: vehicle.fuelType as Car['fuelType'],
    seats: vehicle.seats,
    luggage: vehicle.luggageCapacity,
    image: imageUrl,
    images: imageUrl ? [imageUrl] : [],
    features: vehicle.features || [],
    available: vehicle.isAvailable ?? true,
    rating: vehicle.rating || 0,
    reviews: vehicle.reviewCount || 0,
  };
};

// Transform array or page of vehicles
const transformVehicles = (data: any): Car[] => {
  if (Array.isArray(data)) {
    return data.map(transformVehicleToCar);
  }
  if (data?.content && Array.isArray(data.content)) {
    return data.content.map(transformVehicleToCar);
  }
  if (data && typeof data === 'object' && 'id' in data) {
    return [transformVehicleToCar(data)];
  }
  return [];
};

export const carsAPI = {
  getAvailable: async (search?: string, page: number = 0, size: number = 50) => {
    const response = await axiosInstance.get<any>('/cars/available', {
      params: { search, page, size },
    });
    return {
      ...response,
      data: transformVehicles(response.data),
    };
  },

  getAll: async (filters?: CarFilters) => {
    const response = await axiosInstance.get<any>('/vehicles', { params: { ...filters, available: true } });
    return {
      ...response,
      data: transformVehicles(response.data),
    };
  },
  
  getById: async (id: string) => {
    const response = await axiosInstance.get<VehicleResponse>(`/vehicles/${id}`);
    return {
      ...response,
      data: transformVehicleToCar(response.data),
    };
  },
  
  create: (data: Omit<Car, 'id'>) =>
    axiosInstance.post<Car>('/vehicles', data),
  
  update: (id: string, data: Partial<Car>) =>
    axiosInstance.put<Car>(`/vehicles/${id}`, data),
  
  delete: (id: string) =>
    axiosInstance.delete(`/vehicles/${id}`),
  
  search: async (query: string) => {
    const response = await axiosInstance.get<any>('/vehicles/search', { params: { q: query } });
    return {
      ...response,
      data: transformVehicles(response.data),
    };
  },
};
