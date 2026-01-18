import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search } from 'lucide-react';
import { carsAPI } from '../api/cars';
import type { Car } from '../types/car';
import CarSearchBar from '../components/Car/CarSearchBar';
import CarTable from '../components/Car/CarTable';

const CarListingPage: React.FC = () => {
  const [searchInput, setSearchInput] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  const { data: cars = [], isLoading } = useQuery({
    queryKey: ['cars', 'available', searchQuery],
    queryFn: async (): Promise<Car[]> => {
      const response = await carsAPI.getAvailable(searchQuery);
      // carsAPI.getAvailable already maps to Car[]
      return response.data;
    },
  });

  const handleSearch = () => {
    setSearchQuery(searchInput);
  };

  const availableCars = cars.filter((c) => c.available !== false);

  return (
    <div className="min-h-screen bg-neutral-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white border border-neutral-200 rounded-xl p-6 md:p-8 mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-neutral-900">Car Listing</h1>
          <p className="text-neutral-600 mt-2">Browse available cars and view details</p>

          <div className="mt-6">
            <CarSearchBar
              value={searchInput}
              onChange={setSearchInput}
              onSearch={handleSearch}
              isLoading={isLoading}
            />
          </div>
        </div>

        {isLoading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-neutral-600">Loading cars...</p>
          </div>
        ) : availableCars.length === 0 ? (
          <div className="bg-white border border-neutral-200 rounded-lg p-8 text-center">
            <Search className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
            <p className="text-neutral-600 text-lg">
              {searchQuery && searchQuery.trim() ? 'No cars available matching your search.' : 'No cars available'}
            </p>
          </div>
        ) : (
          <CarTable cars={availableCars} />
        )}
      </div>
    </div>
  );
};

export default CarListingPage;
