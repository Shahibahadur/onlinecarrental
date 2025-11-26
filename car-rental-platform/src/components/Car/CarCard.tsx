import React, { useState } from 'react';
import { Car, Users, Luggage, Fuel, Star, MapPin, Calendar } from 'lucide-react';
import { Car as CarType } from '../../types';

interface CarCardProps {
  car: CarType;
  onBook: (car: CarType) => void;
  onViewDetails: (car: CarType) => void;
}

const CarCard: React.FC<CarCardProps> = ({ car, onBook, onViewDetails }) => {
  const [imageLoaded, setImageLoaded] = useState(false);

  return (
    <div className="bg-white rounded-lg shadow-sm border border-neutral-200 overflow-hidden hover:shadow-md transition-shadow">
      <div className="relative">
        <img
          src={car.image}
          alt={car.name}
          className={`w-full h-48 object-cover ${imageLoaded ? 'opacity-100' : 'opacity-0'} transition-opacity`}
          onLoad={() => setImageLoaded(true)}
        />
        {!imageLoaded && (
          <div className="absolute inset-0 bg-neutral-200 animate-pulse" />
        )}
        {!car.available && (
          <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <span className="text-white font-semibold bg-red-600 px-3 py-1 rounded-full">
              Not Available
            </span>
          </div>
        )}
        <div className="absolute top-3 right-3 bg-white rounded-full px-2 py-1 flex items-center space-x-1">
          <Star className="h-4 w-4 text-yellow-400 fill-current" />
          <span className="text-sm font-medium">{car.rating}</span>
        </div>
      </div>

      <div className="p-4">
        <div className="flex justify-between items-start mb-2">
          <h3 className="text-lg font-semibold text-neutral-900">{car.name}</h3>
          <div className="text-right">
            <div className="text-2xl font-bold text-primary-600">${car.pricePerDay}</div>
            <div className="text-sm text-neutral-500">per day</div>
          </div>
        </div>

        <div className="flex items-center text-neutral-600 mb-3">
          <MapPin className="h-4 w-4 mr-1" />
          <span className="text-sm">{car.location}</span>
        </div>

        <div className="flex items-center justify-between text-sm text-neutral-600 mb-4">
          <div className="flex items-center space-x-4">
            <div className="flex items-center">
              <Users className="h-4 w-4 mr-1" />
              <span>{car.seats} seats</span>
            </div>
            <div className="flex items-center">
              <Luggage className="h-4 w-4 mr-1" />
              <span>{car.luggage} bags</span>
            </div>
            <div className="flex items-center">
              <Fuel className="h-4 w-4 mr-1" />
              <span>{car.fuelType}</span>
            </div>
          </div>
        </div>

        <div className="flex space-x-2">
          <button
            onClick={() => onViewDetails(car)}
            className="flex-1 px-4 py-2 text-sm font-medium text-neutral-700 bg-neutral-100 rounded-md hover:bg-neutral-200 transition-colors"
          >
            View Details
          </button>
          <button
            onClick={() => onBook(car)}
            disabled={!car.available}
            className={`flex-1 px-4 py-2 text-sm font-medium text-white rounded-md transition-colors ${
              car.available
                ? 'bg-primary-600 hover:bg-primary-700'
                : 'bg-neutral-400 cursor-not-allowed'
            }`}
          >
            Book Now
          </button>
        </div>
      </div>
    </div>
  );
};

export default CarCard;