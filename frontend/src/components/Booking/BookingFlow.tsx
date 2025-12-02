import React, { useState } from 'react';
import { X } from 'lucide-react';
import type { Car } from '../../types/car';
import { formatPrice } from '../../utils/formatters';

interface BookingFlowProps {
  car: Car;
  onClose: () => void;
  onBooking: (dates: { startDate: string; endDate: string }) => void;
}

const BookingFlow: React.FC<BookingFlowProps> = ({ car, onClose, onBooking }) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (startDate && endDate) {
      onBooking({ startDate, endDate });
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-md w-full">
        <div className="flex justify-between items-center p-6 border-b border-neutral-200">
          <h2 className="text-xl font-bold text-neutral-900">Book {car.name}</h2>
          <button onClick={onClose} className="text-neutral-500 hover:text-neutral-700">
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Pickup Date
            </label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-neutral-700 mb-2">
              Dropoff Date
            </label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500"
              required
            />
          </div>

          <div className="bg-neutral-50 p-4 rounded-lg">
            <p className="text-sm text-neutral-600">
              Price per day: {formatPrice(car.pricePerDay)}
            </p>
          </div>

          <button
            type="submit"
            className="w-full bg-primary-600 text-white py-2 rounded-lg font-medium hover:bg-primary-700 transition-colors"
          >
            Continue to Checkout
          </button>
        </form>
      </div>
    </div>
  );
};

export default BookingFlow;
