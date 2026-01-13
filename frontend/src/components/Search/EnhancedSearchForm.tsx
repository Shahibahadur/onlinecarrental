import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, Search as SearchIcon } from 'lucide-react';
import type { RootState } from '../../store';
import { setFilters } from '../../store/slices/searchSlice';
import { LOCATIONS } from '../../constants';

const EnhancedSearchForm: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const filters = useSelector((state: RootState) => state.search);
  const [serviceType, setServiceType] = useState<'rent' | 'driver' | 'self-drive'>('rent');

  const handleInputChange = (key: string, value: string) => {
    dispatch(setFilters({ [key]: value }));
  };

  const handleSearch = () => {
    navigate('/cars');
  };

  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];

  return (
    <div className="bg-white rounded-2xl shadow-xl p-6 md:p-8">
      {/* Service Type Selection */}
      <div className="mb-6">
        <h3 className="text-lg font-semibold text-neutral-900 mb-4">Choose a service</h3>
        <div className="grid grid-cols-3 gap-3">
          <button
            onClick={() => setServiceType('rent')}
            className={`px-4 py-3 rounded-lg font-medium transition-all ${
              serviceType === 'rent'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Rent a Car
          </button>
          <button
            onClick={() => setServiceType('driver')}
            className={`px-4 py-3 rounded-lg font-medium transition-all ${
              serviceType === 'driver'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Hire a Driver
          </button>
          <button
            onClick={() => setServiceType('self-drive')}
            className={`px-4 py-3 rounded-lg font-medium transition-all ${
              serviceType === 'self-drive'
                ? 'bg-primary-600 text-white shadow-md'
                : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
            }`}
          >
            Self-Drive
          </button>
        </div>
      </div>

      {/* Search Form */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {/* Pick Up Location */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            <MapPin className="inline h-4 w-4 mr-1" />
            Pick Up Location
          </label>
          <select
            value={filters.pickupLocation || ''}
            onChange={(e) => handleInputChange('pickupLocation', e.target.value)}
            className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent bg-white text-neutral-900 cursor-pointer"
            style={{
              backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
              backgroundPosition: 'right 0.5rem center',
              backgroundRepeat: 'no-repeat',
              backgroundSize: '1.5em 1.5em',
              paddingRight: '2.5rem'
            }}
          >
            <option value="" style={{ color: '#374151', backgroundColor: '#ffffff' }}>Please enter from location</option>
            {LOCATIONS.map((location) => (
              <option 
                key={location} 
                value={location}
                style={{ color: '#111827', backgroundColor: '#ffffff' }}
              >
                {location}
              </option>
            ))}
          </select>
        </div>

        {/* Drop Off Location */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            <MapPin className="inline h-4 w-4 mr-1" />
            Drop Off Location
          </label>
          <select
            value={filters.dropoffLocation || ''}
            onChange={(e) => handleInputChange('dropoffLocation', e.target.value)}
            className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent bg-white text-neutral-900 cursor-pointer"
            style={{
              backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
              backgroundPosition: 'right 0.5rem center',
              backgroundRepeat: 'no-repeat',
              backgroundSize: '1.5em 1.5em',
              paddingRight: '2.5rem'
            }}
          >
            <option value="" style={{ color: '#374151', backgroundColor: '#ffffff' }}>Please enter to location</option>
            {LOCATIONS.map((location) => (
              <option 
                key={location} 
                value={location}
                style={{ color: '#111827', backgroundColor: '#ffffff' }}
              >
                {location}
              </option>
            ))}
          </select>
        </div>

        {/* Pick Up Date */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            <Calendar className="inline h-4 w-4 mr-1" />
            Pick Up Date
          </label>
          <input
            type="date"
            value={filters.startDate || today}
            min={today}
            onChange={(e) => handleInputChange('startDate', e.target.value)}
            className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          />
        </div>

        {/* Drop Date */}
        <div>
          <label className="block text-sm font-medium text-neutral-700 mb-2">
            <Calendar className="inline h-4 w-4 mr-1" />
            Drop Date
          </label>
          <input
            type="date"
            value={filters.endDate || tomorrow}
            min={filters.startDate || tomorrow}
            onChange={(e) => handleInputChange('endDate', e.target.value)}
            className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          />
        </div>
      </div>

      {/* Search Button */}
      <button
        onClick={handleSearch}
        className="w-full bg-primary-600 hover:bg-primary-700 text-white font-semibold py-4 rounded-lg transition-colors flex items-center justify-center space-x-2 shadow-lg hover:shadow-xl"
      >
        <SearchIcon className="h-5 w-5" />
        <span>Find Vehicle</span>
      </button>
    </div>
  );
};

export default EnhancedSearchForm;


