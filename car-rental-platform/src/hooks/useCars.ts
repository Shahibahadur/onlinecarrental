import { useQuery } from 'react-query';
import { Car, SearchFilters } from '../types';
import { mockCars } from '../constants/mockData';

export const useCars = (filters: Partial<SearchFilters>) => {
  return useQuery(['cars', filters], async (): Promise<Car[]> => {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 500));
    
    let filteredCars = mockCars;

    if (filters.location) {
      filteredCars = filteredCars.filter(car => 
        car.location.toLowerCase().includes(filters.location!.toLowerCase())
      );
    }

    if (filters.carType) {
      filteredCars = filteredCars.filter(car => car.type === filters.carType);
    }

    if (filters.transmission) {
      filteredCars = filteredCars.filter(car => car.transmission === filters.transmission);
    }

    if (filters.fuelType) {
      filteredCars = filteredCars.filter(car => car.fuelType === filters.fuelType);
    }

    if (filters.minPrice) {
      filteredCars = filteredCars.filter(car => car.pricePerDay >= filters.minPrice!);
    }

    if (filters.maxPrice) {
      filteredCars = filteredCars.filter(car => car.pricePerDay <= filters.maxPrice!);
    }

    return filteredCars;
  });
};