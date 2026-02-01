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
  registrationNumber?: string;
}

export interface CarFilters {
  carType?: string;
  transmission?: string;
  fuelType?: string;
  minPrice?: number;
  maxPrice?: number;
  location?: string;
  brand?: string;
}
