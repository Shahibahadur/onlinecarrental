import axiosInstance from './axios';
import type { Car } from '../types/car';

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
  description?: string;
  isAvailable: boolean;
  rating?: number;
  reviewCount?: number;
}

const normalizeImageUrl = (url?: string | null) => {
  if (!url) return '';
  const apiBaseUrl = import.meta.env.VITE_API_BASE || 'http://localhost:8080';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) return `${apiBaseUrl}${url}`;
  return url;
};

const normalizeVehicleType = (type?: string): Car['type'] => {
  const t = String(type || '').toUpperCase();
  if (t === 'SUV') return 'SUV';
  if (t === 'SEDAN') return 'Sedan';
  if (t === 'HATCHBACK') return 'Hatchback';
  if (t === 'LUXURY') return 'Luxury';
  if (t === 'SPORTS') return 'Sports';
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
    year: vehicle.year,
    description: vehicle.description || '',
  };
};

const transformVehicles = (data: any): Car[] => {
  if (Array.isArray(data)) return data.map(transformVehicleToCar);
  if (data?.recommendations && Array.isArray(data.recommendations)) {
    return data.recommendations.map(transformVehicleToCar);
  }
  return [];
};

export const recommendationsAPI = {
  getPersonalizedRecommendations: async () => {
    const response = await axiosInstance.get<VehicleResponse[]>('/recommendations/personalized');
    return { ...response, data: transformVehicles(response.data) };
  },
  getHybridRecommendations: async () => {
    const response = await axiosInstance.get<any>('/recommendations/hybrid');
    return { ...response, data: transformVehicles(response.data) };
  },
  getPopularCars: async () => {
    const response = await axiosInstance.get<VehicleResponse[]>('/recommendations/popular');
    return { ...response, data: transformVehicles(response.data) };
  },
};
