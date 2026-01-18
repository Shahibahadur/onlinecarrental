import { useQuery } from '@tanstack/react-query';
import { type Car, type SearchFilters } from '../types';
import { carsAPI } from '../api/cars';

export const useCars = (filters: Partial<SearchFilters>) => {
  return useQuery({
    queryKey: ['cars', filters],
    queryFn: async (): Promise<Car[]> => {
      const resp = await carsAPI.getAvailable(undefined, 0, 200);
      let filteredCars = resp.data;

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
    }
  });
};