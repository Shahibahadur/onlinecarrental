import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Search, Grid, List, Filter } from 'lucide-react';
import type { RootState } from '../store';
import { useCars } from '../hooks/useCars';
import CarCard from '../components/Car/CarCard';
import SearchFilters from '../components/Search/SearchFilters';
import CarDetailModal from '../components/Car/CarDetailModal';
import BookingModal from '../components/Booking/BookingModal';
import type { Car } from '../types';

const Cars: React.FC = () => {
  const navigate = useNavigate();
  const filters = useSelector((state: RootState) => state.search);
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const { data: cars, isLoading, error } = useCars(filters);
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);
  const [bookingCar, setBookingCar] = useState<Car | null>(null);
  const [showFilters, setShowFilters] = useState(false);

  const handleBookClick = (car: Car) => {
    if (!isAuthenticated) {
      // Redirect to login with return URL
      const confirmLogin = window.confirm(
        'You need to login or register to book a car. Do you want to go to the login page?'
      );
      if (confirmLogin) {
        navigate('/login', { state: { returnTo: '/cars', carId: car.id } });
      }
      return;
    }
    setBookingCar(car);
  };

  if (error) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-lg mb-4">Failed to load cars</div>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="bg-white border border-neutral-200 rounded-xl p-6 md:p-8 mb-8">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <div>
              <h1 className="text-3xl md:text-4xl font-bold text-neutral-900">Available Cars in Nepal</h1>
              <p className="text-neutral-600 mt-2 max-w-2xl">
                Find the perfect vehicle for your journey across Nepal
              </p>
            </div>

            <div className="flex items-center space-x-4">
              {/* View Toggle */}
              <div className="flex bg-neutral-50 rounded-lg border border-neutral-300 p-1">
                <button
                  onClick={() => setViewMode('grid')}
                  className={`p-2 rounded ${
                    viewMode === 'grid'
                      ? 'bg-primary-100 text-primary-600'
                      : 'text-neutral-600'
                  }`}
                >
                  <Grid className="h-4 w-4" />
                </button>
                <button
                  onClick={() => setViewMode('list')}
                  className={`p-2 rounded ${
                    viewMode === 'list'
                      ? 'bg-primary-100 text-primary-600'
                      : 'text-neutral-600'
                  }`}
                >
                  <List className="h-4 w-4" />
                </button>
              </div>

              {/* Mobile Filter Button */}
              <button
                onClick={() => setShowFilters(!showFilters)}
                className="md:hidden flex items-center px-4 py-2 border border-neutral-300 rounded-lg bg-white text-neutral-700 hover:bg-neutral-50"
              >
                <Filter className="h-4 w-4 mr-2" />
                Filters
              </button>
            </div>
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Filters Sidebar */}
          <div className={`lg:w-80 ${showFilters ? 'block' : 'hidden lg:block'}`}>
            <SearchFilters />
          </div>

          {/* Cars Grid */}
          <div className="flex-1">
            {isLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <div key={i} className="bg-white rounded-xl shadow-sm border border-neutral-200 overflow-hidden">
                    <div className="h-48 bg-neutral-200 animate-pulse" />
                    <div className="p-4 space-y-3">
                      <div className="h-4 bg-neutral-200 rounded animate-pulse" />
                      <div className="h-4 bg-neutral-200 rounded animate-pulse w-2/3" />
                      <div className="h-10 bg-neutral-200 rounded animate-pulse" />
                    </div>
                  </div>
                ))}
              </div>
            ) : cars && cars.length > 0 ? (
              <div className={
                viewMode === 'grid'
                  ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6'
                  : 'space-y-6'
              }>
                {cars.map((car) => (
                  <CarCard
                    key={car.id}
                    car={car}
                    onBook={handleBookClick}
                    onViewDetails={setSelectedCar}
                  />
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <Search className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-neutral-900 mb-2">
                  No cars found
                </h3>
                <p className="text-neutral-600">
                  Try adjusting your search filters to find more results.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modals */}
      {selectedCar && (
        <CarDetailModal
          car={selectedCar}
          isOpen={!!selectedCar}
          onClose={() => setSelectedCar(null)}
          onBook={() => {
            handleBookClick(selectedCar);
            setSelectedCar(null);
          }}
        />
      )}

      {bookingCar && isAuthenticated && bookingCar.id && bookingCar.pricePerDay && (
        <BookingModal
          car={bookingCar}
          isOpen={!!bookingCar}
          onClose={() => setBookingCar(null)}
          onSuccess={() => {
            setBookingCar(null);
            // Optionally navigate to dashboard to see the booking
            navigate('/dashboard');
          }}
        />
      )}
    </div>
  );
};

export default Cars;