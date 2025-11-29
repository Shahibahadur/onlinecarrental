import {type  Car } from '../types';
import { CARS_CDN_BASE_URL, LOCATIONS } from '.';

// Popular car models in Nepal with realistic NPR pricing
export const mockCars: Car[] = [
  {
    id: '1',
    name: 'Toyota Vitz',
    model: 'Vitz',
    brand: 'Toyota',
    type: 'Hatchback',
    pricePerDay: 3500, // NPR
    location: LOCATIONS[0], // Kathmandu
    transmission: 'Automatic',
    fuelType: 'Petrol',
    seats: 5,
    luggage: 2,
    image: `${CARS_CDN_BASE_URL}/photo-1621135802920-133df287f89c?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1621135802920-133df287f89c?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Bluetooth', 'Air Conditioning', 'GPS Navigation', 'Fuel Efficient', 'Mountain Ready'],
    available: true,
    rating: 4.6,
    reviews: 142,
  },
  {
    id: '2',
    name: 'Hyundai Creta',
    model: 'Creta',
    brand: 'Hyundai',
    type: 'SUV',
    pricePerDay: 6500, // NPR
    location: LOCATIONS[1], // Pokhara
    transmission: 'Automatic',
    fuelType: 'Diesel',
    seats: 5,
    luggage: 4,
    image: `${CARS_CDN_BASE_URL}/photo-1555215695-3004980ad54e?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['High Ground Clearance', 'All-Wheel Drive', 'Touchscreen Infotainment', 'Reverse Camera', 'Mountain Terrain Ready'],
    available: true,
    rating: 4.7,
    reviews: 98,
  },
  {
    id: '3',
    name: 'Suzuki Swift',
    model: 'Swift',
    brand: 'Suzuki',
    type: 'Hatchback',
    pricePerDay: 3200, // NPR
    location: LOCATIONS[0], // Kathmandu
    transmission: 'Manual',
    fuelType: 'Petrol',
    seats: 5,
    luggage: 2,
    image: `${CARS_CDN_BASE_URL}/photo-1560958089-b8a1929cea89?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1560958089-b8a1929cea89?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Fuel Efficient', 'Compact Design', 'Easy Parking', 'Air Conditioning', 'City Friendly'],
    available: true,
    rating: 4.5,
    reviews: 187,
  },
  {
    id: '4',
    name: 'Toyota Hiace',
    model: 'Hiace',
    brand: 'Toyota',
    type: 'SUV',
    pricePerDay: 8500, // NPR
    location: LOCATIONS[2], // Chitwan
    transmission: 'Manual',
    fuelType: 'Diesel',
    seats: 12,
    luggage: 6,
    image: `${CARS_CDN_BASE_URL}/photo-1549317661-bd32c8ce0db2?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1549317661-bd32c8ce0db2?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Large Seating Capacity', 'Tourist Group Friendly', 'Luggage Space', 'Reliable Engine', 'Long Distance Ready'],
    available: true,
    rating: 4.8,
    reviews: 156,
  },
  {
    id: '5',
    name: 'Mahindra Scorpio',
    model: 'Scorpio',
    brand: 'Mahindra',
    type: 'SUV',
    pricePerDay: 7500, // NPR
    location: LOCATIONS[1], // Pokhara
    transmission: 'Manual',
    fuelType: 'Diesel',
    seats: 7,
    luggage: 4,
    image: `${CARS_CDN_BASE_URL}/photo-1563720223480-9f5a7a2a5a3e?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1563720223480-9f5a7a2a5a3e?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Rugged Design', 'High Ground Clearance', '4WD Capable', 'Powerful Engine', 'Off-Road Ready'],
    available: true,
    rating: 4.6,
    reviews: 124,
  },
  {
    id: '6',
    name: 'Honda City',
    model: 'City',
    brand: 'Honda',
    type: 'Sedan',
    pricePerDay: 4500, // NPR
    location: LOCATIONS[0], // Kathmandu
    transmission: 'Automatic',
    fuelType: 'Petrol',
    seats: 5,
    luggage: 3,
    image: `${CARS_CDN_BASE_URL}/photo-1503376780353-7e6692767b70?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1503376780353-7e6692767b70?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Comfortable Ride', 'Premium Interior', 'Touchscreen Display', 'Safety Features', 'Smooth Driving'],
    available: true,
    rating: 4.7,
    reviews: 203,
  },
  {
    id: '7',
    name: 'Tata Nexon',
    model: 'Nexon',
    brand: 'Tata',
    type: 'SUV',
    pricePerDay: 5500, // NPR
    location: LOCATIONS[3], // Lalitpur
    transmission: 'Manual',
    fuelType: 'Diesel',
    seats: 5,
    luggage: 3,
    image: `${CARS_CDN_BASE_URL}/photo-1552519507-da3b142c6e3d?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1552519507-da3b142c6e3d?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Modern Design', 'Good Mileage', 'Touchscreen', 'Safety Rating', 'Value for Money'],
    available: true,
    rating: 4.4,
    reviews: 89,
  },
  {
    id: '8',
    name: 'Toyota Corolla',
    model: 'Corolla',
    brand: 'Toyota',
    type: 'Sedan',
    pricePerDay: 5000, // NPR
    location: LOCATIONS[4], // Bhaktapur
    transmission: 'Automatic',
    fuelType: 'Petrol',
    seats: 5,
    luggage: 3,
    image: `${CARS_CDN_BASE_URL}/photo-1606664515525-efd81ee1b5c1?w=500&h=300&fit=crop`,
    images: [
      `${CARS_CDN_BASE_URL}/photo-1606664515525-efd81ee1b5c1?w=800&h=600&fit=crop`,
      `${CARS_CDN_BASE_URL}/photo-1544636331-e26879cd4d9b?w=800&h=600&fit=crop`,
    ],
    features: ['Reliable', 'Comfortable', 'Fuel Efficient', 'Spacious', 'Popular Choice'],
    available: false,
    rating: 4.8,
    reviews: 234,
  },
];