import React, { useState } from 'react';
import { X, Calendar, MapPin } from 'lucide-react';
import { Car } from '../../types';

interface BookingModalProps {
  car: Car;
  isOpen: boolean;
  onClose: () => void;
}

const BookingModal: React.FC<BookingModalProps> = ({ car, isOpen, onClose }) => {
  const [bookingData, setBookingData] = useState({
    startDate: '',
    endDate: '',
    pickupLocation: car.location,
    dropoffLocation: car.location,
  });

  const calculateTotal = () => {
    if (!bookingData.startDate || !bookingData.endDate) return 0;
    const start = new Date(bookingData.startDate);
    const end = new Date(bookingData.endDate);
    const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    return days * car.pricePerDay;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Handle booking logic here
    console.log('Booking submitted:', { car, ...bookingData, total: calculateTotal() });
    alert('Booking confirmed successfully!');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" onClick={onClose} />

        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="absolute top-4 right-4">
            <button
              onClick={onClose}
              className="bg-white rounded-full p-2 hover:bg-neutral-100 transition-colors"
            >
              <X className="h-5 w-5 text-neutral-600" />
            </button>
          </div>

          <div className="bg-white px-6 pt-6 pb-6">
            <h2 className="text-2xl font-bold text-neutral-900 mb-2">Book {car.name}</h2>
            <p className="text-neutral-600 mb-6">Complete your booking details</p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-neutral-700 mb-1">
                    Start Date
                  </label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-neutral-400" />
                    <input
                      type="date"
                      required
                      value={bookingData.startDate}
                      onChange={(e) => setBookingData(prev => ({ ...prev, startDate: e.target.value }))}
                      className="w-full pl-10 pr-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-neutral-700 mb-1">
                    End Date
                  </label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-neutral-400" />
                    <input
                      type="date"
                      required
                      value={bookingData.endDate}
                      onChange={(e) => setBookingData(prev => ({ ...prev, endDate: e.target.value }))}
                      className="w-full pl-10 pr-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Pickup Location
                </label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-neutral-400" />
                  <input
                    type="text"
                    required
                    value={bookingData.pickupLocation}
                    onChange={(e) => setBookingData(prev => ({ ...prev, pickupLocation: e.target.value }))}
                    className="w-full pl-10 pr-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Drop-off Location
                </label>
                <div className="relative">
                  <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-neutral-400" />
                  <input
                    type="text"
                    required
                    value={bookingData.dropoffLocation}
                    onChange={(e) => setBookingData(prev => ({ ...prev, dropoffLocation: e.target.value }))}
                    className="w-full pl-10 pr-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  />
                </div>
              </div>

              {/* Price Summary */}
              <div className="bg-neutral-50 p-4 rounded-lg">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-neutral-600">Daily Rate</span>
                  <span className="font-medium">${car.pricePerDay}/day</span>
                </div>
                {bookingData.startDate && bookingData.endDate && (
                  <>
                    <div className="flex justify-between items-center mb-2">
                      <span className="text-neutral-600">Rental