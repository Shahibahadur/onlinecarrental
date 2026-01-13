import React from 'react';
import { Car } from 'lucide-react';

const Footer: React.FC = () => {
  return (
    <footer className="bg-neutral-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center space-x-2 mb-4 md:mb-0">
            <Car className="h-6 w-6 text-primary-400" />
            <span className="text-lg font-bold">Car Rental Website</span>
          </div>
          <div className="text-neutral-400 text-sm">
            <p>&copy; {new Date().getFullYear()} Car Rental Website. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
