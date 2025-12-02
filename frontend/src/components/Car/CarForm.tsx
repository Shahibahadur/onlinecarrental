import React from 'react';
import { useForm } from 'react-hook-form';
import type { Car } from '../../types/car';
import { CAR_TYPES, TRANSMISSION_TYPES, FUEL_TYPES } from '../../utils/constants';

interface CarFormProps {
  car?: Car;
  onSubmit: (data: Partial<Car>) => void;
  isLoading?: boolean;
}

const CarForm: React.FC<CarFormProps> = ({ car, onSubmit, isLoading = false }) => {
  const { register, handleSubmit } = useForm({
    defaultValues: car,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Name</label>
          <input
            type="text"
            {...register('name', { required: 'Name is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Model</label>
          <input
            type="text"
            {...register('model', { required: 'Model is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Brand</label>
          <input
            type="text"
            {...register('brand', { required: 'Brand is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Type</label>
          <select
            {...register('type', { required: 'Type is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          >
            <option value="">Select Type</option>
            {CAR_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Price Per Day</label>
          <input
            type="number"
            {...register('pricePerDay', { required: 'Price is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Seats</label>
          <input
            type="number"
            {...register('seats', { required: 'Seats is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Transmission</label>
          <select
            {...register('transmission', { required: 'Transmission is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          >
            <option value="">Select Transmission</option>
            {TRANSMISSION_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Fuel Type</label>
          <select
            {...register('fuelType', { required: 'Fuel Type is required' })}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          >
            <option value="">Select Fuel Type</option>
            {FUEL_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="w-full bg-primary-600 text-white py-2 rounded-lg font-medium hover:bg-primary-700 disabled:opacity-50"
      >
        {isLoading ? 'Saving...' : 'Save Car'}
      </button>
    </form>
  );
};

export default CarForm;
