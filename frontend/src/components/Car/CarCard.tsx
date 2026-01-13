import React, { useState } from 'react';
import { Users, Luggage, Fuel, Star, MapPin } from 'lucide-react';
import {type Car as CarType } from '../../types';
import { formatPricePerDay } from '../../constants/locale';

interface CarCardProps {
  car: CarType;
  onBook: (car: CarType) => void;
  onViewDetails: (car: CarType) => void;
}

const CarCard: React.FC<CarCardProps> = ({ car, onBook, onViewDetails }) => {
  const [imageLoaded, setImageLoaded] = useState(false);

  return (
    <div className="bg-white rounded-xl shadow-sm border border-neutral-200 overflow-hidden hover:shadow-md hover:-translate-y-0.5 transition-all">
      <div className="relative">
        <img
          src={car.image}
          alt={car.name}
          className={`w-full h-56 object-cover ${imageLoaded ? 'opacity-100' : 'opacity-0'} transition-opacity`}
          onLoad={() => setImageLoaded(true)}
        />
        {!imageLoaded && (
          <div className="absolute inset-0 bg-neutral-200 animate-pulse" />
        )}

        {!car.available && (
          <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <span className="text-white font-semibold bg-red-600 px-3 py-1 rounded-full">
              उपलब्ध छैन (Not Available)
            </span>
          </div>
        )}
        <div className="absolute top-3 left-3 bg-white/95 backdrop-blur rounded-full px-3 py-1 text-sm font-semibold text-primary-700">
          {formatPricePerDay(car.pricePerDay)}
        </div>
        <div className="absolute top-3 right-3 bg-white/95 backdrop-blur rounded-full px-2 py-1 flex items-center space-x-1">
          <Star className="h-4 w-4 text-yellow-400 fill-current" />
          <span className="text-sm font-medium">{car.rating}</span>
        </div>
      </div>

      <div className="p-4">
        <h3 className="text-lg font-semibold text-neutral-900 leading-tight">{car.name}</h3>

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
            className="flex-1 px-4 py-2 text-sm font-semibold text-neutral-800 bg-neutral-100 rounded-lg hover:bg-neutral-200 transition-colors"
          >
            View Details
          </button>
          <button
            onClick={() => onBook(car)}
            disabled={!car.available}
            className={`flex-1 px-4 py-2 text-sm font-semibold text-white rounded-lg transition-colors ${
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