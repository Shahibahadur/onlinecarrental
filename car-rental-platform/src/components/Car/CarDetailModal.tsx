import React, { useState } from 'react';
import { X, Users, Luggage, Fuel, MapPin, Star, ChevronLeft, ChevronRight } from 'lucide-react';
import { Car } from '../../types';

interface CarDetailModalProps {
  car: Car;
  isOpen: boolean;
  onClose: () => void;
  onBook: () => void;
}

const CarDetailModal: React.FC<CarDetailModalProps> = ({
  car,
  isOpen,
  onClose,
  onBook,
}) => {
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  if (!isOpen) return null;

  const nextImage = () => {
    setCurrentImageIndex((prev) => 
      prev === car.images.length - 1 ? 0 : prev + 1
    );
  };

  const prevImage = () => {
    setCurrentImageIndex((prev) => 
      prev === 0 ? car.images.length - 1 : prev - 1
    );
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose} />

        {/* Modal panel */}
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full">
          <div className="absolute top-4 right-4">
            <button
              onClick={onClose}
              className="bg-white rounded-full p-2 hover:bg-neutral-100 transition-colors"
            >
              <X className="h-5 w-5 text-neutral-600" />
            </button>
          </div>

          <div className="bg-white">
            {/* Image Gallery */}
            <div className="relative h-80 sm:h-96">
              <img
                src={car.images[currentImageIndex]}
                alt={car.name}
                className="w-full h-full object-cover"
              />
              
              {car.images.length > 1 && (
                <>
                  <button
                    onClick={prevImage}
                    className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-lg hover:bg-neutral-100 transition-colors"
                  >
                    <ChevronLeft className="h-5 w-5" />
                  </button>
                  <button
                    onClick={nextImage}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-lg hover:bg-neutral-100 transition-colors"
                  >
                    <ChevronRight className="h-5 w-5" />
                  </button>
                </>
              )}
              
              <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-2">
                {car.images.map((_, index) => (
                  <button
                    key={index}
                    onClick={() => setCurrentImageIndex(index)}
                    className={`w-2 h-2 rounded-full ${
                      index === currentImageIndex ? 'bg-white' : 'bg-white bg-opacity-50'
                    }`}
                  />
                ))}
              </div>
            </div>

            {/* Content */}
            <div className="p-6">
              <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between mb-4">
                <div>
                  <h2 className="text-2xl font-bold text-neutral-900">{car.name}</h2>
                  <div className="flex items-center mt-2 space-x-4 text-sm text-neutral-600">
                    <div className="flex items-center">
                      <MapPin className="h-4 w-4 mr-1" />
                      {car.location}
                    </div>
                    <div className="flex items-center">
                      <Star className="h-4 w-4 mr-1 text-yellow-400 fill-current" />
                      {car.rating} ({car.reviews} reviews)
                    </div>
                  </div>
                </div>
                <div className="mt-4 sm:mt-0 text-right">
                  <div className="text-3xl font-bold text-primary-600">${car.pricePerDay}</div>
                  <div className="text-sm text-neutral-500">per day</div>
                </div>
              </div>

              {/* Specifications */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div className="flex items-center">
                  <Users className="h-5 w-5 text-neutral-400 mr-2" />
                  <div>
                    <div className="text-sm text-neutral-600">Seats</div>
                    <div className="font-medium">{car.seats} people</div>
                  </div>
                </div>
                <div className="flex items-center">
                  <Luggage className="h-5 w-5 text-neutral-400 mr-2" />
                  <div>
                    <div className="text-sm text-neutral-600">Luggage</div>
                    <div className="font-medium">{car.luggage} bags</div>
                  </div>
                </div>
                <div className="flex items-center">
                  <Fuel className="h-5 w-5 text-neutral-400 mr-2" />
                  <div>
                    <div className="text-sm text-neutral-600">Fuel</div>
                    <div className="font-medium">{car.fuelType}</div>
                  </div>
                </div>
                <div className="flex items-center">
                  <div className="w-5 h-5 text-neutral-400 mr-2 flex items-center justify-center">
                    ⚙️
                  </div>
                  <div>
                    <div className="text-sm text-neutral-600">Transmission</div>
                    <div className="font-medium">{car.transmission}</div>
                  </div>
                </div>
              </div>

              {/* Features */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-neutral-900 mb-3">Features</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                  {car.features.map((feature, index) => (
                    <div key={index} className="flex items-center">
                      <div className="w-2 h-2 bg-primary-600 rounded-full mr-2" />
                      <span className="text-sm text-neutral-600">{feature}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex space-x-4">
                <button
                  onClick={onClose}
                  className="flex-1 px-6 py-3 border border-neutral-300 text-neutral-700 rounded-md hover:bg-neutral-50 transition-colors font-medium"
                >
                  Close
                </button>
                <button
                  onClick={onBook}
                  disabled={!car.available}
                  className={`flex-1 px-6 py-3 text-white rounded-md font-medium transition-colors ${
                    car.available
                      ? 'bg-primary-600 hover:bg-primary-700'
                      : 'bg-neutral-400 cursor-not-allowed'
                  }`}
                >
                  {car.available ? 'Book This Car' : 'Not Available'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CarDetailModal;