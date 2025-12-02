import React from 'react';
import CarCard from '../Car/CarCard';
import type { Car } from '../../types/car';

interface RecommendationWidgetProps {
  cars: Car[];
  title?: string;
  onBook: (car: Car) => void;
  onViewDetails: (car: Car) => void;
}

const RecommendationWidget: React.FC<RecommendationWidgetProps> = ({
  cars,
  title = 'Recommended for You',
  onBook,
  onViewDetails,
}) => {
  if (cars.length === 0) return null;

  return (
    <section className="py-8">
      <h2 className="text-2xl font-bold text-neutral-900 mb-6">{title}</h2>
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
    </section>
  );
};

export default RecommendationWidget;
