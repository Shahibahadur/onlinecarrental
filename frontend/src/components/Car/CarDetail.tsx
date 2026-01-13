import React from 'react';
import type { Car } from '../../types/car';
import { formatPricePerDay } from '../../constants/locale';
import { Users, Luggage, Fuel, Zap } from 'lucide-react';

interface CarDetailProps {
  car: Car;
  onBook: () => void;
}

const CarDetail: React.FC<CarDetailProps> = ({ car, onBook }) => {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="mb-6">
        <img src={car.image} alt={car.name} className="w-full h-96 object-cover rounded-lg" />
      </div>

      <div className="space-y-4">
        <div>
          <h1 className="text-3xl font-bold text-neutral-900">{car.name}</h1>
          <p className="text-neutral-600">{car.model}</p>
        </div>

        <div className="flex items-center justify-between py-4 border-t border-b border-neutral-200">
          <div>
            <p className="text-neutral-600 text-sm">Price per Day</p>
            <p className="text-2xl font-bold text-primary-600">{formatPricePerDay(car.pricePerDay)}</p>
          </div>
          <div>
            <p className="text-neutral-600 text-sm">Rating</p>
            <p className="text-2xl font-bold text-yellow-500">★ {car.rating}</p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 py-4">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5 text-neutral-600" />
            <span>{car.seats} Seats</span>
          </div>
          <div className="flex items-center gap-2">
            <Luggage className="h-5 w-5 text-neutral-600" />
            <span>{car.luggage} Luggage</span>
          </div>
          <div className="flex items-center gap-2">
            <Fuel className="h-5 w-5 text-neutral-600" />
            <span>{car.fuelType}</span>
          </div>
          <div className="flex items-center gap-2">
            <Zap className="h-5 w-5 text-neutral-600" />
            <span>{car.transmission}</span>
          </div>
        </div>

        <div>
          <h3 className="font-semibold text-neutral-900 mb-2">Features</h3>
          <ul className="grid grid-cols-2 gap-2">
            {car.features.map((feature, idx) => (
              <li key={idx} className="text-neutral-600 text-sm">✓ {feature}</li>
            ))}
          </ul>
        </div>

        <button
          onClick={onBook}
          className="w-full bg-primary-600 text-white py-3 rounded-lg font-medium hover:bg-primary-700 transition-colors"
        >
          Book Now
        </button>
      </div>
    </div>
  );
};

export default CarDetail;
