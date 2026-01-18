import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { adminAPI } from '../../api/admin';
import type { Car } from '../../types/car';
import { CAR_TYPES, TRANSMISSION_TYPES, FUEL_TYPES, LOCATIONS } from '../../constants';

interface CarFormProps {
  car?: Car;
  onSubmit?: (data: Partial<Car>) => void;
  onSuccess?: () => void;
  onCancel?: () => void;
  isLoading?: boolean;
}

const CarForm: React.FC<CarFormProps> = ({ car, onSubmit, onSuccess, onCancel, isLoading: externalLoading }) => {
  const queryClient = useQueryClient();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState<any>(() =>
    car || {
      name: '',
      model: '',
      brand: '',
      type: 'Sedan',
      pricePerDay: 0,
      location: 'Kathmandu',
      transmission: 'Manual',
      fuelType: 'Petrol',
      seats: 5,
      luggage: 2,
      image: '',
      images: [],
      features: '',
      available: true,
      rating: 5,
      reviews: 0,
    }
  );

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isUploading, setIsUploading] = useState(false);

  const createMutation = useMutation({
    mutationFn: (data: any) => adminAPI.createVehicle(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicles'] });
      queryClient.invalidateQueries({ queryKey: ['cars'] });
      if (onSuccess) onSuccess();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: any) => adminAPI.updateVehicle(car!.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicles'] });
      queryClient.invalidateQueries({ queryKey: ['cars'] });
      if (onSuccess) onSuccess();
    },
  });

  const onSubmitForm = async (data: any) => {
    setIsSubmitting(true);
    try {
      if (!data.name) {
        setErrors({ name: 'Name is required' });
        return;
      }
      if (!data.model) {
        setErrors({ model: 'Model is required' });
        return;
      }
      if (!data.brand) {
        setErrors({ brand: 'Brand is required' });
        return;
      }
      if (!data.image) {
        setErrors({ image: 'Image URL is required' });
        return;
      }

      setErrors({});

      const payload: any = {
        ...data,
        pricePerDay: Number(data.pricePerDay || 0),
        seats: Number(data.seats || 0),
        luggage: Number(data.luggage || 0),
        features: typeof data.features === 'string'
          ? data.features.split(',').map((f: string) => f.trim()).filter((f: string) => f)
          : Array.isArray(data.features) ? data.features : [],
        images: data.image ? [data.image] : [],
      };
      
      if (onSubmit) {
        onSubmit(payload);
      } else {
        if (car) {
          updateMutation.mutate(payload);
        } else {
          createMutation.mutate(payload);
        }
      }
    } catch (error) {
      console.error('Form submission error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const isLoading = externalLoading || isSubmitting || createMutation.isPending || updateMutation.isPending;

  const handleChange = (key: string, value: any) => {
    setFormData((prev: any) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmitForm(formData);
  };

  return (
    <form onSubmit={handleFormSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Name</label>
          <input
            type="text"
            value={formData.name}
            onChange={(e) => handleChange('name', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
          {errors.name && (
            <p className="text-red-600 text-xs mt-1">{errors.name}</p>
          )}
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Model</label>
          <input
            type="text"
            value={formData.model}
            onChange={(e) => handleChange('model', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
          {errors.model && (
            <p className="text-red-600 text-xs mt-1">{errors.model}</p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Brand</label>
          <input
            type="text"
            value={formData.brand}
            onChange={(e) => handleChange('brand', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          />
          {errors.brand && (
            <p className="text-red-600 text-xs mt-1">{errors.brand}</p>
          )}
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Type</label>
          <select
            value={formData.type}
            onChange={(e) => handleChange('type', e.target.value)}
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
          <label className="block text-sm font-medium text-neutral-700 mb-1">Price Per Day (रू)</label>
          <input
            type="number"
            value={formData.pricePerDay}
            onChange={(e) => handleChange('pricePerDay', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Seats</label>
          <input
            type="number"
            value={formData.seats}
            onChange={(e) => handleChange('seats', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Location</label>
          <select
            value={formData.location}
            onChange={(e) => handleChange('location', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            {LOCATIONS.map(location => (
              <option key={location} value={location}>{location}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Luggage Capacity</label>
          <input
            type="number"
            value={formData.luggage}
            onChange={(e) => handleChange('luggage', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-neutral-700 mb-1">Image URL</label>
        <input
          type="url"
          value={formData.image}
          onChange={(e) => handleChange('image', e.target.value)}
          placeholder="https://example.com/car-image.jpg"
          className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
        {errors.image && (
          <p className="text-red-600 text-xs mt-1">{errors.image}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-neutral-700 mb-1">Upload Image</label>
        <input
          type="file"
          accept="image/*"
          disabled={isLoading || isUploading}
          onChange={async (e) => {
            const file = e.target.files?.[0];
            if (!file) return;

            try {
              setIsUploading(true);
              const resp = await adminAPI.uploadVehicleImage(file, String(formData.type || 'general'));
              handleChange('image', resp.data);
              setErrors((prev) => {
                const next = { ...prev };
                delete next.image;
                return next;
              });
            } finally {
              setIsUploading(false);
            }
          }}
          className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-neutral-700 mb-1">Features (comma-separated)</label>
        <input
          type="text"
          placeholder="GPS, Bluetooth, Air Conditioning"
          value={typeof formData.features === 'string' ? formData.features : (car?.features?.join(', ') || '')}
          onChange={(e) => handleChange('features', e.target.value)}
          className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
        <p className="text-xs text-neutral-500 mt-1">Enter features separated by commas</p>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={formData.available !== false}
              onChange={(e) => handleChange('available', e.target.checked)}
              className="mr-2"
            />
            <span className="text-sm font-medium text-neutral-700">Available for booking</span>
          </label>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-1">Transmission</label>
          <select
            value={formData.transmission}
            onChange={(e) => handleChange('transmission', e.target.value)}
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
            value={formData.fuelType}
            onChange={(e) => handleChange('fuelType', e.target.value)}
            className="w-full px-4 py-2 border border-neutral-300 rounded-lg"
          >
            <option value="">Select Fuel Type</option>
            {FUEL_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="flex space-x-4 pt-4">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg font-medium hover:bg-neutral-50 transition-colors"
          >
            Cancel
          </button>
        )}
        <button
          type="submit"
          disabled={isLoading}
          className="flex-1 bg-primary-600 text-white py-2 rounded-lg font-medium hover:bg-primary-700 disabled:opacity-50 transition-colors"
        >
          {isLoading ? 'Saving...' : car ? 'Update Vehicle' : 'Create Vehicle'}
        </button>
      </div>
    </form>
  );
};

export default CarForm;
