import React, { useState } from 'react';
import CarDetail from '../../components/Car/CarDetail';
import type { Car } from '../../types/car';

const CarDetailPage: React.FC = () => {
  const [car] = useState<Car | null>(null);

  const handleBook = () => {
    console.log('Book car');
    // Implement booking logic
  };

  if (!car) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-neutral-600">Car not found</p>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <CarDetail car={car} onBook={handleBook} />
    </div>
  );
};

export default CarDetailPage;
