import React, { useState } from 'react';
import { CreditCard } from 'lucide-react';
import type { BookingRequest } from '../../types/api';

interface CheckoutFormProps {
  booking: BookingRequest;
  totalPrice: number;
  onSubmit: (paymentData: any) => void;
  isLoading?: boolean;
}

const CheckoutForm: React.FC<CheckoutFormProps> = ({ totalPrice, onSubmit, isLoading = false }) => {
  const [cardNumber, setCardNumber] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [cvv, setCvv] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      cardNumber,
      expiryDate,
      cvv,
      amount: totalPrice,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="bg-neutral-50 p-4 rounded-lg">
        <h3 className="font-semibold text-neutral-900 mb-2">Order Summary</h3>
        <div className="space-y-1 text-sm text-neutral-600">
          <p>Subtotal: ${totalPrice.toFixed(2)}</p>
          <p className="font-semibold text-neutral-900 text-lg mt-2">Total: ${totalPrice.toFixed(2)}</p>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-neutral-700 mb-2">
          Card Number
        </label>
        <input
          type="text"
          placeholder="1234 5678 9012 3456"
          value={cardNumber}
          onChange={(e) => setCardNumber(e.target.value)}
          className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500"
          required
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            Expiry Date
          </label>
          <input
            type="text"
            placeholder="MM/YY"
            value={expiryDate}
            onChange={(e) => setExpiryDate(e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            CVV
          </label>
          <input
            type="text"
            placeholder="123"
            value={cvv}
            onChange={(e) => setCvv(e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            required
          />
        </div>
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="w-full bg-primary-600 text-white py-2 rounded-lg font-medium hover:bg-primary-700 disabled:opacity-50 flex items-center justify-center gap-2 transition-colors"
      >
        <CreditCard className="h-4 w-4" />
        {isLoading ? 'Processing...' : 'Complete Payment'}
      </button>
    </form>
  );
};

export default CheckoutForm;
