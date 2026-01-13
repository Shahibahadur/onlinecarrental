import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Search, Sparkles } from 'lucide-react';
import { carsAPI } from '../api/cars';
import { recommendationsAPI } from '../api/recommendations';
import type { Car } from '../types/car';
import { formatPricePerDay } from '../constants/locale';
import { useSelector } from 'react-redux';
import type { RootState } from '../store';

const CarsTable: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);

  // Fetch available cars
  const { data: carsResponse, isLoading } = useQuery({
    queryKey: ['cars', 'available', searchQuery],
    queryFn: async () => {
      if (searchQuery && searchQuery.trim()) {
        // Use search endpoint if search query exists - backend already filters by available=true
        const response = await carsAPI.search(searchQuery.trim());
        return response.data;
      } else {
        // Get all available cars - backend already filters by available=true
        const response = await carsAPI.getAll();
        return response.data;
      }
    },
  });

  const cars = Array.isArray(carsResponse) ? carsResponse : [];
  // Filter to show only available cars (backend should already filter, but this is a safety check)
  const availableCars = cars.filter((car: Car) => car.available !== false);

  // Fetch personalized recommendations if user is authenticated
  const { data: recommendedCars } = useQuery({
    queryKey: ['recommendations', 'personalized'],
    queryFn: async () => {
      const response = await recommendationsAPI.getPersonalizedRecommendations();
      return response.data;
    },
    enabled: isAuthenticated && !!user, // Only fetch if user is authenticated
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes
  });

  // Filter cars based on search term
  const filteredCars = availableCars.filter((car: Car) => {
    if (!searchTerm) return true;
    const query = searchTerm.toLowerCase();
    return (
      car.name?.toLowerCase().includes(query) ||
      car.brand?.toLowerCase().includes(query) ||
      car.model?.toLowerCase().includes(query) ||
      car.type?.toLowerCase().includes(query) ||
      car.location?.toLowerCase().includes(query)
    );
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchQuery(searchTerm);
  };

  return (
    <div className="min-h-screen bg-white py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header with Search Bar */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900 mb-6">Car Listing</h1>
          
          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex gap-3 mb-4">
            <div className="flex-1 relative">
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search for cars by name, brand, model, type, or location..."
                className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <button
              type="submit"
              className="px-6 py-3 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors flex items-center gap-2"
            >
              <Search className="h-5 w-5" />
              Search
            </button>
          </form>

          <p className="text-neutral-600">
            Showing {filteredCars.length} available {filteredCars.length === 1 ? 'car' : 'cars'}
          </p>
          {isAuthenticated && recommendedCars && recommendedCars.length > 0 && (
            <div className="mt-2 flex items-center gap-2 text-primary-600">
              <Sparkles className="h-4 w-4" />
              <span className="text-sm font-medium">Personalized recommendations available based on your preferences</span>
            </div>
          )}
        </div>

        {/* Car Listing Table */}
        {isLoading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-neutral-600">Loading cars...</p>
          </div>
        ) : filteredCars.length === 0 ? (
          <div className="bg-white border border-neutral-200 rounded-lg p-8 text-center">
            <p className="text-neutral-600 text-lg">
              {searchTerm ? 'No cars found matching your search.' : 'No available cars at the moment.'}
            </p>
            {searchTerm && (
              <button
                onClick={() => {
                  setSearchTerm('');
                  setSearchQuery('');
                }}
                className="mt-4 text-primary-600 hover:text-primary-700 underline"
              >
                Clear search
              </button>
            )}
          </div>
        ) : (
          <div className="bg-white border border-neutral-200 rounded-lg overflow-hidden">
            <table className="w-full">
              <thead className="bg-neutral-50 border-b border-neutral-200">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Image</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Car Details</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Type</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Location</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Price/Day</th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {filteredCars.map((car: Car) => (
                  <tr key={car.id} className="hover:bg-neutral-50">
                    <td className="px-6 py-4">
                      <img
                        src={car.image || 'https://via.placeholder.com/150'}
                        alt={car.name}
                        className="h-20 w-32 object-cover rounded"
                      />
                    </td>
                    <td className="px-6 py-4">
                      <div>
                        <div className="font-semibold text-neutral-900">{car.name}</div>
                        <div className="text-sm text-neutral-600">{car.brand} {car.model}</div>
                        <div className="text-xs text-neutral-500 mt-1">
                          {car.seats} seats • {car.transmission} • {car.fuelType}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-sm text-neutral-700">{car.type}</span>
                    </td>
                    <td className="px-6 py-4 text-neutral-600">{car.location}</td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-neutral-900">
                        {formatPricePerDay(car.pricePerDay)}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <Link
                        to={`/cars/${car.id}`}
                        className="px-4 py-2 bg-primary-600 text-white rounded text-sm font-medium hover:bg-primary-700 transition-colors inline-block"
                      >
                        View Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default CarsTable;
