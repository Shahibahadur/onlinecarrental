import React from 'react';
import CarCard from './CarCard';
import type { Car } from '../../types/car';

interface CarListProps {
  cars: Car[];
  onBook: (car: Car) => void;
  onViewDetails: (car: Car) => void;
}

const CarList: React.FC<CarListProps> = ({ cars, onBook, onViewDetails }) => {
  if (cars.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-neutral-600 text-lg">No cars found</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {cars.map((car) => (
        <CarCard
          key={car.id}
          car={car}
          onBook={() => onBook(car)}
          onViewDetails={() => onViewDetails(car)}
        />
      ))}
    </div>
  );
};

export default CarList;
