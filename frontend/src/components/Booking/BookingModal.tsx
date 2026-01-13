import React, { useState, useEffect, useMemo } from 'react';
import { X, Calendar, MapPin, User, CreditCard, Shield, AlertCircle, CheckCircle, ArrowRight, ArrowLeft, Calculator, Info, Percent } from 'lucide-react';
import { useSelector } from 'react-redux';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { type Car } from '../../types';
import { formatCurrency, formatPricePerDay } from '../../constants/locale';
import { LOCATIONS } from '../../constants';
import { bookingAPI } from '../../api/booking';
import type { RootState } from '../../store';
import type { BookingRequest } from '../../types/api';
import { useNavigate } from 'react-router-dom';

interface BookingModalProps {
  car: Car;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

type BookingStep = 'details' | 'options' | 'confirm';

const BookingModal: React.FC<BookingModalProps> = ({ car, isOpen, onClose, onSuccess }) => {
  const { user } = useSelector((state: RootState) => state.auth);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [currentStep, setCurrentStep] = useState<BookingStep>('details');

  const [bookingData, setBookingData] = useState({
    startDate: '',
    endDate: '',
    pickupLocation: car?.location || '',
    dropoffLocation: car?.location || '',
    withDriver: false,
    insurance: false,
    paymentMethod: 'cash',
    customerName: user?.firstName && user?.lastName ? `${user.firstName} ${user.lastName}` : '',
    customerPhone: user?.phone || '',
    customerEmail: user?.email || '',
  });

  // Update user info when user changes
  useEffect(() => {
    if (user) {
      setBookingData(prev => ({
        ...prev,
        customerName: user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : prev.customerName,
        customerPhone: user.phone || prev.customerPhone,
        customerEmail: user.email || prev.customerEmail,
      }));
    }
  }, [user]);

  // Reset form when modal opens/closes
  useEffect(() => {
    if (isOpen && car) {
      setError(null);
      setSuccess(false);
      setCurrentStep('details');
      const today = new Date().toISOString().split('T')[0];
      const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
      setBookingData(prev => ({
        ...prev,
        startDate: prev.startDate || today,
        endDate: prev.endDate || tomorrow,
        pickupLocation: prev.pickupLocation || car.location || '',
        dropoffLocation: prev.dropoffLocation || car.location || '',
      }));
    }
  }, [isOpen, car]);

  const bookingMutation = useMutation({
    mutationFn: (data: BookingRequest) => bookingAPI.create(data),
    onSuccess: (resp) => {
      setSuccess(true);
      queryClient.invalidateQueries({ queryKey: ['userBookings'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });

      const bookingId = (resp as any)?.data?.id;
      const paymentMethod = bookingData.paymentMethod;

      if (paymentMethod === 'esewa' && bookingId) {
        setTimeout(() => {
          onClose();
          navigate(`/esewa/checkout?bookingId=${bookingId}`);
        }, 800);
        return;
      }

      setTimeout(() => {
        if (onSuccess) {
          onSuccess();
        } else {
          onClose();
        }
      }, 2000);
    },
    onError: (err: any) => {
      console.error('Booking error:', err);
      let errorMessage = 'Failed to create booking. Please try again.';
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.response?.data?.error) {
        errorMessage = err.response.data.error;
      } else if (err.message) {
        errorMessage = err.message;
      }
      setError(errorMessage);
    },
  });

  const calculateDays = () => {
    if (!bookingData.startDate || !bookingData.endDate) return 0;
    const start = new Date(bookingData.startDate);
    const end = new Date(bookingData.endDate);
    const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    return days > 0 ? days : 1; // Minimum 1 day
  };

  // Advanced Pricing Algorithm
  const pricingBreakdown = useMemo(() => {
    const days = calculateDays();
    if (days === 0) {
      return {
        basePrice: 0,
        dailyRate: car.pricePerDay,
        days: 0,
        subtotal: 0,
        longTermDiscount: 0,
        discountPercentage: 0,
        weekendSurcharge: 0,
        oneWayFee: 0,
        driverCost: 0,
        insuranceCost: 0,
        tax: 0,
        total: 0,
        weekendDays: 0,
      };
    }

    const baseDailyRate = car.pricePerDay;
    let subtotal = days * baseDailyRate;
    let longTermDiscount = 0;
    let discountPercentage = 0;
    let weekendSurcharge = 0;
    let oneWayFee = 0;

    // Long-term discount algorithm
    if (days >= 30) {
      discountPercentage = 25; // 25% off for 30+ days
      longTermDiscount = subtotal * 0.25;
    } else if (days >= 14) {
      discountPercentage = 15; // 15% off for 14+ days
      longTermDiscount = subtotal * 0.15;
    } else if (days >= 7) {
      discountPercentage = 10; // 10% off for 7+ days
      longTermDiscount = subtotal * 0.10;
    } else if (days >= 3) {
      discountPercentage = 5; // 5% off for 3+ days
      longTermDiscount = subtotal * 0.05;
    }

    // Calculate weekend/holiday surcharge
    const startDate = new Date(bookingData.startDate);
    const endDate = new Date(bookingData.endDate);
    let weekendDays = 0;

    for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
      const dayOfWeek = d.getDay();
      if (dayOfWeek === 0 || dayOfWeek === 6) { // Saturday or Sunday
        weekendDays++;
      }
    }

    // Weekend surcharge: 15% extra for weekend days
    if (weekendDays > 0) {
      const weekendSubtotal = (weekendDays / days) * subtotal;
      weekendSurcharge = weekendSubtotal * 0.15;
    }

    // One-way rental fee (if pickup != dropoff)
    if (bookingData.pickupLocation && bookingData.dropoffLocation && 
        bookingData.pickupLocation !== bookingData.dropoffLocation) {
      oneWayFee = 2000; // रू 2,000 one-way fee
    }

    // Apply discounts
    subtotal = subtotal - longTermDiscount + weekendSurcharge + oneWayFee;

    // Driver cost
    const driverCost = bookingData.withDriver ? days * 1500 : 0;

    // Insurance cost
    const insuranceCost = bookingData.insurance ? days * 500 : 0;

    // Tax (13% VAT as per Nepal regulations)
    const beforeTax = subtotal + driverCost + insuranceCost;
    const tax = beforeTax * 0.13;

    // Final total
    const total = beforeTax + tax;

    return {
      basePrice: car.pricePerDay,
      dailyRate: baseDailyRate,
      days,
      subtotal: days * baseDailyRate,
      longTermDiscount,
      discountPercentage,
      weekendSurcharge,
      oneWayFee,
      driverCost,
      insuranceCost,
      tax,
      total: Math.round(total),
      weekendDays,
    };
  }, [bookingData, car.pricePerDay]);

  const canProceedToStep2 = () => {
    return bookingData.startDate && 
           bookingData.endDate && 
           bookingData.pickupLocation && 
           bookingData.dropoffLocation &&
           pricingBreakdown.days > 0;
  };

  const handleStep1Next = () => {
    if (!canProceedToStep2()) {
      setError('Please fill in all required fields: dates and locations');
      return;
    }
    setError(null);
    setCurrentStep('options');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    // Validate dates
    const startDate = new Date(bookingData.startDate);
    const endDate = new Date(bookingData.endDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (startDate < today) {
      setError('Start date cannot be in the past');
      return;
    }

    if (endDate <= startDate) {
      setError('End date must be after start date');
      return;
    }

    // Prepare booking request
    const bookingRequest: BookingRequest = {
      vehicleId: car.id,
      startDate: bookingData.startDate,
      endDate: bookingData.endDate,
      pickupLocation: bookingData.pickupLocation,
      dropoffLocation: bookingData.dropoffLocation,
    };

    // Create booking
    bookingMutation.mutate(bookingRequest);
  };

  if (!isOpen || !car) {
    return null;
  }

  // Ensure car has required properties
  if (!car.id || !car.pricePerDay || !car.name) {
    console.error('BookingModal: Invalid car data', car);
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
        <div className="bg-white rounded-lg p-6 max-w-md">
          <p className="text-red-600 mb-4">Error: Invalid car information</p>
          <button
            onClick={onClose}
            className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
          >
            Close
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto" role="dialog" aria-modal="true" aria-labelledby="booking-modal-title">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" 
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Modal Container */}
      <div className="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
        {/* Modal Panel */}
        <div className="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-4xl">
          {/* Close Button */}
          <div className="absolute right-4 top-4 z-10">
            <button
              onClick={onClose}
              className="rounded-full bg-white p-2 text-neutral-600 shadow-md hover:bg-neutral-100 transition-colors"
              aria-label="Close modal"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Step Indicator */}
          <div className="bg-primary-50 px-6 py-4 border-b border-primary-200">
            <div className="flex items-center justify-center space-x-4">
              <div className={`flex items-center ${currentStep === 'details' ? 'text-primary-600' : 'text-neutral-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${currentStep === 'details' ? 'bg-primary-600 text-white' : 'bg-neutral-300'}`}>
                  1
                </div>
                <span className="ml-2 font-medium">Details</span>
              </div>
              <ArrowRight className="h-5 w-5 text-neutral-300" />
              <div className={`flex items-center ${currentStep === 'options' ? 'text-primary-600' : 'text-neutral-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${currentStep === 'options' ? 'bg-primary-600 text-white' : 'bg-neutral-300'}`}>
                  2
                </div>
                <span className="ml-2 font-medium">Options</span>
              </div>
              <ArrowRight className="h-5 w-5 text-neutral-300" />
              <div className={`flex items-center ${currentStep === 'confirm' ? 'text-primary-600' : 'text-neutral-400'}`}>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center ${currentStep === 'confirm' ? 'bg-primary-600 text-white' : 'bg-neutral-300'}`}>
                  3
                </div>
                <span className="ml-2 font-medium">Confirm</span>
              </div>
            </div>
          </div>

          {/* Modal Content */}
          <div className="bg-white px-6 pt-6 pb-6 max-h-[80vh] overflow-y-auto">
            {/* Success Message */}
            {success && (
              <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-center space-x-3">
                <CheckCircle className="h-5 w-5 text-green-600" />
                <div>
                  <p className="text-green-800 font-medium">Booking confirmed successfully!</p>
                  <p className="text-green-600 text-sm">Redirecting to your dashboard...</p>
                </div>
              </div>
            )}

            {/* Error Message */}
            {error && !success && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-center space-x-3">
                <AlertCircle className="h-5 w-5 text-red-600" />
                <div>
                  <p className="text-red-800 font-medium">Booking Failed</p>
                  <p className="text-red-600 text-sm">{error}</p>
                </div>
              </div>
            )}

            {/* STEP 1: Details & Pricing */}
            {currentStep === 'details' && (
              <div className="space-y-6">
                <div>
                  <h2 className="text-2xl font-bold text-neutral-900 mb-2">Book {car.name}</h2>
                  <p className="text-neutral-600">Select your rental dates and locations</p>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Left: Form */}
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-neutral-700 mb-1">
                          <Calendar className="inline h-4 w-4 mr-1" />
                          Start Date
                        </label>
                        <input
                          type="date"
                          required
                          min={new Date().toISOString().split('T')[0]}
                          value={bookingData.startDate}
                          onChange={(e) => {
                            setBookingData(prev => ({ ...prev, startDate: e.target.value }));
                            setError(null);
                          }}
                          className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-neutral-700 mb-1">
                          <Calendar className="inline h-4 w-4 mr-1" />
                          End Date
                        </label>
                        <input
                          type="date"
                          required
                          min={bookingData.startDate || new Date().toISOString().split('T')[0]}
                          value={bookingData.endDate}
                          onChange={(e) => {
                            setBookingData(prev => ({ ...prev, endDate: e.target.value }));
                            setError(null);
                          }}
                          className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-neutral-700 mb-1">
                        <MapPin className="inline h-4 w-4 mr-1" />
                        Pickup Location
                      </label>
                      <select
                        required
                        value={bookingData.pickupLocation}
                        onChange={(e) => {
                          setBookingData(prev => ({ ...prev, pickupLocation: e.target.value }));
                          setError(null);
                        }}
                        className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                      >
                        <option value="">Select pickup location</option>
                        {LOCATIONS.map((location) => (
                          <option key={location} value={location}>
                            {location}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-neutral-700 mb-1">
                        <MapPin className="inline h-4 w-4 mr-1" />
                        Drop-off Location
                      </label>
                      <select
                        required
                        value={bookingData.dropoffLocation}
                        onChange={(e) => {
                          setBookingData(prev => ({ ...prev, dropoffLocation: e.target.value }));
                          setError(null);
                        }}
                        className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                      >
                        <option value="">Select drop-off location</option>
                        {LOCATIONS.map((location) => (
                          <option key={location} value={location}>
                            {location}
                          </option>
                        ))}
                      </select>
                    </div>

                    {pricingBreakdown.days > 0 && (
                      <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                        <div className="flex items-center space-x-2 text-blue-800">
                          <Info className="h-4 w-4" />
                          <span className="text-sm font-medium">
                            Rental Duration: {pricingBreakdown.days} {pricingBreakdown.days === 1 ? 'day' : 'days'}
                            {pricingBreakdown.weekendDays > 0 && ` (${pricingBreakdown.weekendDays} weekend ${pricingBreakdown.weekendDays === 1 ? 'day' : 'days'})`}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Right: Pricing Breakdown */}
                  <div className="bg-gradient-to-br from-primary-50 to-blue-50 rounded-lg p-6 border border-primary-200">
                    <div className="flex items-center space-x-2 mb-4">
                      <Calculator className="h-5 w-5 text-primary-600" />
                      <h3 className="text-lg font-bold text-neutral-900">Price Calculation</h3>
                    </div>

                    {pricingBreakdown.days === 0 ? (
                      <div className="text-center py-8 text-neutral-500">
                        <p>Fill in dates and locations to see pricing</p>
                      </div>
                    ) : (
                      <div className="space-y-3">
                        <div className="flex justify-between text-sm">
                          <span className="text-neutral-600">Base Rate</span>
                          <span className="font-medium">{formatPricePerDay(pricingBreakdown.dailyRate)}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-neutral-600">× {pricingBreakdown.days} days</span>
                          <span className="font-medium">{formatCurrency(pricingBreakdown.subtotal)}</span>
                        </div>

                        {pricingBreakdown.discountPercentage > 0 && (
                          <div className="flex justify-between text-sm text-green-600 pt-2 border-t border-neutral-200">
                            <span className="flex items-center">
                              <Percent className="h-4 w-4 mr-1" />
                              {pricingBreakdown.discountPercentage}% Long-term Discount
                            </span>
                            <span className="font-medium">-{formatCurrency(pricingBreakdown.longTermDiscount)}</span>
                          </div>
                        )}

                        {pricingBreakdown.weekendSurcharge > 0 && (
                          <div className="flex justify-between text-sm text-orange-600">
                            <span>Weekend Surcharge ({pricingBreakdown.weekendDays} days)</span>
                            <span className="font-medium">+{formatCurrency(pricingBreakdown.weekendSurcharge)}</span>
                          </div>
                        )}

                        {pricingBreakdown.oneWayFee > 0 && (
                          <div className="flex justify-between text-sm text-orange-600">
                            <span>One-way Rental Fee</span>
                            <span className="font-medium">+{formatCurrency(pricingBreakdown.oneWayFee)}</span>
                          </div>
                        )}

                        <div className="pt-3 border-t-2 border-primary-200">
                          <div className="flex justify-between items-center mb-2">
                            <span className="font-semibold text-neutral-900">Subtotal</span>
                            <span className="font-bold text-lg text-primary-600">
                              {formatCurrency(pricingBreakdown.total - pricingBreakdown.driverCost - pricingBreakdown.insuranceCost - pricingBreakdown.tax)}
                            </span>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex justify-end space-x-3 pt-4 border-t">
                  <button
                    type="button"
                    onClick={onClose}
                    className="px-6 py-2 border border-neutral-300 text-neutral-700 rounded-md hover:bg-neutral-50 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    onClick={handleStep1Next}
                    disabled={!canProceedToStep2()}
                    className={`px-6 py-2 rounded-md font-medium transition-colors flex items-center ${
                      canProceedToStep2()
                        ? 'bg-primary-600 text-white hover:bg-primary-700'
                        : 'bg-neutral-300 text-neutral-500 cursor-not-allowed'
                    }`}
                  >
                    Continue
                    <ArrowRight className="h-4 w-4 ml-2" />
                  </button>
                </div>
              </div>
            )}

            {/* STEP 2: Additional Options */}
            {currentStep === 'options' && (
              <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                  <h2 className="text-2xl font-bold text-neutral-900 mb-2">Add Services</h2>
                  <p className="text-neutral-600">Choose additional services for your rental</p>
                </div>

                {/* Driver Option */}
                <div className="border border-neutral-200 rounded-lg p-4 hover:border-primary-300 transition-colors">
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={bookingData.withDriver}
                      onChange={(e) => setBookingData(prev => ({ ...prev, withDriver: e.target.checked }))}
                      className="w-5 h-5 text-primary-600 rounded focus:ring-primary-500"
                    />
                    <User className="h-5 w-5 ml-3 text-neutral-600" />
                    <div className="ml-3 flex-1">
                      <span className="font-medium text-neutral-900">Include Professional Driver</span>
                      <p className="text-sm text-neutral-600">रू 1,500 per day - Experienced drivers who know Nepal roads</p>
                    </div>
                    <span className="text-primary-600 font-semibold">{formatCurrency(pricingBreakdown.driverCost)}</span>
                  </label>
                </div>

                {/* Insurance Option */}
                <div className="border border-neutral-200 rounded-lg p-4 hover:border-primary-300 transition-colors">
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={bookingData.insurance}
                      onChange={(e) => setBookingData(prev => ({ ...prev, insurance: e.target.checked }))}
                      className="w-5 h-5 text-primary-600 rounded focus:ring-primary-500"
                    />
                    <Shield className="h-5 w-5 ml-3 text-neutral-600" />
                    <div className="ml-3 flex-1">
                      <span className="font-medium text-neutral-900">Full Insurance Coverage</span>
                      <p className="text-sm text-neutral-600">रू 500 per day - Comprehensive insurance for peace of mind</p>
                    </div>
                    <span className="text-primary-600 font-semibold">{formatCurrency(pricingBreakdown.insuranceCost)}</span>
                  </label>
                </div>

                {/* Final Price Summary */}
                <div className="bg-gradient-to-br from-primary-50 to-blue-50 rounded-lg p-6 border-2 border-primary-300">
                  <h3 className="text-lg font-bold text-neutral-900 mb-4 flex items-center">
                    <Calculator className="h-5 w-5 mr-2 text-primary-600" />
                    Final Price Breakdown
                  </h3>
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-neutral-600">Subtotal</span>
                      <span>{formatCurrency(pricingBreakdown.total - pricingBreakdown.driverCost - pricingBreakdown.insuranceCost - pricingBreakdown.tax)}</span>
                    </div>
                    {pricingBreakdown.driverCost > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="text-neutral-600">Driver Service</span>
                        <span>{formatCurrency(pricingBreakdown.driverCost)}</span>
                      </div>
                    )}
                    {pricingBreakdown.insuranceCost > 0 && (
                      <div className="flex justify-between text-sm">
                        <span className="text-neutral-600">Insurance</span>
                        <span>{formatCurrency(pricingBreakdown.insuranceCost)}</span>
                      </div>
                    )}
                    <div className="flex justify-between text-sm">
                      <span className="text-neutral-600">VAT (13%)</span>
                      <span>{formatCurrency(pricingBreakdown.tax)}</span>
                    </div>
                    <div className="pt-3 border-t-2 border-primary-300 mt-3">
                      <div className="flex justify-between items-center">
                        <span className="text-xl font-bold text-neutral-900">Total Amount</span>
                        <span className="text-2xl font-bold text-primary-600">{formatCurrency(pricingBreakdown.total)}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Customer Information */}
                <div className="space-y-4 border-t pt-4">
                  <h3 className="font-semibold text-neutral-900">Contact Information</h3>
                  <div>
                    <label className="block text-sm font-medium text-neutral-700 mb-1">Full Name</label>
                    <input
                      type="text"
                      required
                      value={bookingData.customerName}
                      onChange={(e) => setBookingData(prev => ({ ...prev, customerName: e.target.value }))}
                      className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-neutral-700 mb-1">Phone Number</label>
                      <input
                        type="tel"
                        required
                        value={bookingData.customerPhone}
                        onChange={(e) => setBookingData(prev => ({ ...prev, customerPhone: e.target.value }))}
                        placeholder="+977-9800000000"
                        className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-neutral-700 mb-1">Email</label>
                      <input
                        type="email"
                        required
                        value={bookingData.customerEmail}
                        onChange={(e) => setBookingData(prev => ({ ...prev, customerEmail: e.target.value }))}
                        className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
                      />
                    </div>
                  </div>
                </div>

                {/* Payment Method */}
                <div className="border-t pt-4">
                  <label className="block text-sm font-medium text-neutral-700 mb-3">
                    <CreditCard className="inline h-4 w-4 mr-2" />
                    Payment Method
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    {['cash', 'esewa', 'khalti', 'bank'].map((method) => (
                      <label key={method} className="flex items-center p-3 border border-neutral-300 rounded-lg cursor-pointer hover:bg-neutral-50">
                        <input
                          type="radio"
                          name="paymentMethod"
                          value={method}
                          checked={bookingData.paymentMethod === method}
                          onChange={(e) => setBookingData(prev => ({ ...prev, paymentMethod: e.target.value }))}
                          className="mr-2"
                        />
                        <span className="capitalize">{method === 'esewa' ? 'eSewa' : method === 'khalti' ? 'Khalti' : method === 'bank' ? 'Bank Transfer' : 'Cash on Pickup'}</span>
                      </label>
                    ))}
                  </div>
                </div>

                <div className="flex justify-between space-x-3 pt-4 border-t">
                  <button
                    type="button"
                    onClick={() => setCurrentStep('details')}
                    className="px-6 py-2 border border-neutral-300 text-neutral-700 rounded-md hover:bg-neutral-50 transition-colors flex items-center"
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back
                  </button>
                  <button
                    type="submit"
                    disabled={bookingMutation.isPending || success}
                    className={`px-6 py-2 rounded-md font-medium transition-colors ${
                      bookingMutation.isPending || success
                        ? 'bg-neutral-400 text-white cursor-not-allowed'
                        : 'bg-primary-600 text-white hover:bg-primary-700'
                    }`}
                  >
                    {bookingMutation.isPending ? (
                      <span className="flex items-center">
                        <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                        Processing...
                      </span>
                    ) : success ? (
                      <span className="flex items-center">
                        <CheckCircle className="h-5 w-5 mr-2" />
                        Booking Confirmed!
                      </span>
                    ) : (
                      `Confirm Booking - ${formatCurrency(pricingBreakdown.total)}`
                    )}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingModal;
