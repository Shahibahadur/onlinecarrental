/**
 * Car Type-Based Image Management Types
 * Organized by vehicle type (Sedan, SUV, Hatchback, etc.)
 */

export type VehicleType = 
  | 'SEDAN'
  | 'SUV'
  | 'HATCHBACK'
  | 'LUXURY'
  | 'SPORTS'
  | 'ELECTRIC'
  | 'HYBRID'
  | 'VAN'
  | 'TRUCK'
  | 'CONVERTIBLE';

export const VEHICLE_TYPE_DISPLAY: Record<VehicleType, string> = {
  SEDAN: 'Sedan',
  SUV: 'SUV',
  HATCHBACK: 'Hatchback',
  LUXURY: 'Luxury',
  SPORTS: 'Sports',
  ELECTRIC: 'Electric',
  HYBRID: 'Hybrid',
  VAN: 'Van',
  TRUCK: 'Truck',
  CONVERTIBLE: 'Convertible',
};

export interface VehicleImage {
  filename: string;
  vehicleType: VehicleType;
  url: string; // API path
  uploadedAt?: string;
}

export interface VehicleImageUploadRequest {
  file: File;
  vehicleType: VehicleType;
}

// Organized images by vehicle type
export interface ImagesByVehicleType {
  [key in VehicleType]?: VehicleImage[];
}
