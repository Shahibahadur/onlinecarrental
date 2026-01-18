import React, { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search } from 'lucide-react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import SearchBar from '../../components/Search/SearchBar';
import SearchFilters from '../../components/Search/SearchFilters';
import CarCard from '../../components/Car/CarCard';
import BookingModal from '../../components/Booking/BookingModal';
import { carsAPI } from '../../api/cars';
import type { RootState } from '../../store';
import type { Car } from '../../types';

const SearchResults: React.FC = () => {
  const navigate = useNavigate();
  const filters = useSelector((state: RootState) => state.search);
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const [searchQuery, setSearchQuery] = useState('');
  const [bookingCar, setBookingCar] = useState<Car | null>(null);

  const handleSearch = (query: string) => {
    setSearchQuery(query);
  };

  const handleBook = (car: Car) => {
    if (!isAuthenticated) {
      const confirmLogin = window.confirm(
        'You need to login or register to book a car. Do you want to go to the login page?'
      );
      if (confirmLogin) {
        navigate('/login', { state: { returnTo: '/search', carId: car.id } });
      }
      return;
    }
    setBookingCar(car);
  };

  const handleViewDetails = (car: Car) => {
    navigate(`/cars/${car.id}`);
  };

  const { data: cars = [], isLoading, error } = useQuery({
    queryKey: ['search', searchQuery],
    queryFn: async (): Promise<Car[]> => {
      const resp = await carsAPI.search(searchQuery);
      return resp.data;
    },
  });

  const filteredCars = useMemo(() => {
    let list = cars;

    if (filters.location) {
      list = list.filter((c) => c.location.toLowerCase().includes(filters.location.toLowerCase()));
    }
    if (filters.carType) {
      list = list.filter((c) => c.type === filters.carType);
    }
    if (filters.transmission) {
      list = list.filter((c) => c.transmission === filters.transmission);
    }
    if (filters.fuelType) {
      list = list.filter((c) => c.fuelType === filters.fuelType);
    }
    if (filters.minPrice) {
      list = list.filter((c) => c.pricePerDay >= filters.minPrice);
    }
    if (filters.maxPrice) {
      list = list.filter((c) => c.pricePerDay <= filters.maxPrice);
    }

    return list;
  }, [cars, filters]);

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white border border-neutral-200 rounded-xl p-6 md:p-8 mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-neutral-900">Search Cars</h1>
          <p className="text-neutral-600 mt-2">Search and filter available vehicles</p>
          <div className="mt-6">
            <SearchBar onSearch={handleSearch} placeholder="Search by brand, model, type, or location..." />
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          <aside className="lg:w-80">
            <SearchFilters />
          </aside>

          <main className="flex-1">
            {error ? (
              <div className="bg-white border border-neutral-200 rounded-xl p-8 text-center">
                <div className="text-red-600 text-lg mb-2">Failed to load search results</div>
                <button
                  onClick={() => window.location.reload()}
                  className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
                >
                  Try Again
                </button>
              </div>
            ) : isLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <div key={i} className="bg-white rounded-xl shadow-sm border border-neutral-200 overflow-hidden">
                    <div className="h-56 bg-neutral-200 animate-pulse" />
                    <div className="p-4 space-y-3">
                      <div className="h-4 bg-neutral-200 rounded animate-pulse" />
                      <div className="h-4 bg-neutral-200 rounded animate-pulse w-2/3" />
                      <div className="h-10 bg-neutral-200 rounded animate-pulse" />
                    </div>
                  </div>
                ))}
              </div>
            ) : filteredCars.length === 0 ? (
              <div className="text-center py-12">
                <Search className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-neutral-900 mb-2">No cars found</h3>
                <p className="text-neutral-600">Try changing your search or filters.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredCars.map((car) => (
                  <CarCard key={car.id} car={car} onBook={handleBook} onViewDetails={handleViewDetails} />
                ))}
              </div>
            )}
          </main>
        </div>
      </div>

      {bookingCar && isAuthenticated && bookingCar.id && bookingCar.pricePerDay && (
        <BookingModal
          car={bookingCar}
          isOpen={!!bookingCar}
          onClose={() => setBookingCar(null)}
          onSuccess={() => {
            setBookingCar(null);
            navigate('/dashboard');
          }}
        />
      )}
    </div>
  );
};

export default SearchResults;
