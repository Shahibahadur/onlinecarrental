import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Search, Filter } from 'lucide-react';
import type { RootState } from '../../store';
import { setFilters } from '../../store/slices/searchSlice';
import { CAR_TYPES, TRANSMISSION_TYPES, FUEL_TYPES, LOCATIONS } from '../../constants';

const SearchFilters: React.FC = () => {
  const dispatch = useDispatch();
  const filters = useSelector((state: RootState) => state.search);

  const handleFilterChange = (key: string, value: string | number) => {
    dispatch(setFilters({ [key]: value }));
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200">
      <div className="flex items-center mb-4">
        <Filter className="h-5 w-5 text-neutral-600 mr-2" />
        <h3 className="text-lg font-semibold text-neutral-900">Search Filters</h3>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Location */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">
            Location
          </label>
          <select
            value={filters.location}
            onChange={(e) => handleFilterChange('location', e.target.value)}
            className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All Locations</option>
            {LOCATIONS.map((location) => (
              <option key={location} value={location}>
                {location}
              </option>
            ))}
          </select>
        </div>

        {/* Car Type */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">
            Car Type
          </label>
          <select
            value={filters.carType}
            onChange={(e) => handleFilterChange('carType', e.target.value)}
            className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All Types</option>
            {CAR_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>

        {/* Transmission */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">
            Transmission
          </label>
          <select
            value={filters.transmission}
            onChange={(e) => handleFilterChange('transmission', e.target.value)}
            className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All</option>
            {TRANSMISSION_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>

        {/* Fuel Type */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">
            Fuel Type
          </label>
          <select
            value={filters.fuelType}
            onChange={(e) => handleFilterChange('fuelType', e.target.value)}
            className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">All</option>
            {FUEL_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Price Range */}
      <div className="mt-4">
        <label className="block text-sm font-medium text-neutral-700 mb-2">
          Price Range: ${filters.minPrice} - ${filters.maxPrice}
        </label>
        <div className="flex space-x-4">
          <input
            type="range"
            min="0"
            max="1000"
            step="10"
            value={filters.minPrice}
            onChange={(e) => handleFilterChange('minPrice', Number(e.target.value))}
            className="w-full"
          />
          <input
            type="range"
            min="0"
            max="1000"
            step="10"
            value={filters.maxPrice}
            onChange={(e) => handleFilterChange('maxPrice', Number(e.target.value))}
            className="w-full"
          />
        </div>
      </div>
    </div>
  );
};

export default SearchFilters;