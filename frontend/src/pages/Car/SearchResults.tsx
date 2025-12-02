import React, { useState } from 'react';
import SearchBar from '../../components/Search/SearchBar';
import SearchFilters from '../../components/Search/SearchFilters';
import CarList from '../../components/Car/CarList';
import type { Car } from '../../types/car';

const SearchResults: React.FC = () => {
  const [cars, setCars] = useState<Car[]>([]);
  const [filters, setFilters] = useState({
    carType: '',
    minPrice: 0,
    maxPrice: 15000,
    transmission: '',
    fuelType: '',
  });

  const handleSearch = (query: string) => {
    console.log('Search:', query);
    // Implement search logic
  };

  const handleBook = (car: Car) => {
    console.log('Book:', car);
    // Implement booking logic
  };

  const handleViewDetails = (car: Car) => {
    console.log('View details:', car);
    // Navigate to car detail page
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <SearchBar onSearch={handleSearch} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <aside className="lg:col-span-1">
          <SearchFilters filters={filters} onFilterChange={setFilters} />
        </aside>

        <main className="lg:col-span-3">
          <CarList
            cars={cars}
            onBook={handleBook}
            onViewDetails={handleViewDetails}
          />
        </main>
      </div>
    </div>
  );
};

export default SearchResults;
