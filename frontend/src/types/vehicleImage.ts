/**
 * Vehicle Image Types for Frontend
 * Types for the categorized vehicle image system
 */

export type ImageCategory = 
  | 'MAIN' 
  | 'EXTERIOR' 
  | 'INTERIOR' 
  | 'FEATURES' 
  | 'SAFETY' 
  | 'AMENITIES' 
  | 'PERFORMANCE';

export const IMAGE_CATEGORIES: Record<ImageCategory, string> = {
  MAIN: 'Main Display Image',
  EXTERIOR: 'Exterior Views',
  INTERIOR: 'Interior Views',
  FEATURES: 'Feature Highlights',
  SAFETY: 'Safety Features',
  AMENITIES: 'Amenities',
  PERFORMANCE: 'Performance & Specs',
};

export interface VehicleImageResponse {
  id: number;
  vehicleId: number;
  imageName: string;
  imageUrl: string;
  category: ImageCategory;
  displayOrder: number;
  altText?: string;
  description?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface VehicleImageRequest {
  vehicleId: number;
  imageName: string;
  category: ImageCategory;
  displayOrder?: number;
  altText?: string;
  description?: string;
  isActive?: boolean;
}

export interface CategorizedVehicleImages {
  vehicleId: number;
  mainImage?: VehicleImageResponse;
  exteriorImages: VehicleImageResponse[];
  interiorImages: VehicleImageResponse[];
  featureImages: VehicleImageResponse[];
  safetyImages: VehicleImageResponse[];
  amenityImages: VehicleImageResponse[];
  performanceImages: VehicleImageResponse[];
}

// Grouping by category
export interface ImagesByCategory {
  [key in ImageCategory]?: VehicleImageResponse[];
}
