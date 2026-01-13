import React from 'react';
import { Search } from 'lucide-react';

interface CarSearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  isLoading?: boolean;
}

const CarSearchBar: React.FC<CarSearchBarProps> = ({ value, onChange, onSearch, isLoading }) => {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSearch();
      }}
      className="flex gap-3"
    >
      <div className="flex-1 relative">
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Search by name, brand, type, or location..."
          className="w-full px-4 py-3 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>
      <button
        type="submit"
        disabled={!!isLoading}
        className="px-6 py-3 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 disabled:opacity-60 transition-colors flex items-center gap-2"
      >
        <Search className="h-5 w-5" />
        Search
      </button>
    </form>
  );
};

export default CarSearchBar;
