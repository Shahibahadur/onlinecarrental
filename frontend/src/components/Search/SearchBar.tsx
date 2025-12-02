import React, { useState } from 'react';
import { Search } from 'lucide-react';

interface SearchBarProps {
  onSearch: (query: string) => void;
  placeholder?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({ onSearch, placeholder = 'Search cars...' }) => {
  const [query, setQuery] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSearch(query);
  };

  return (
    <form onSubmit={handleSubmit} className="w-full">
      <div className="relative">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={placeholder}
          className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
        />
        <button
          type="submit"
          className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-500 hover:text-primary-600"
        >
          <Search className="h-5 w-5" />
        </button>
      </div>
    </form>
  );
};

export default SearchBar;
