import React from 'react';
import { Link } from 'react-router-dom';
import type { Car } from '../../types/car';
import { formatPricePerDay } from '../../constants/locale';

interface CarTableProps {
  cars: Car[];
}

const CarTable: React.FC<CarTableProps> = ({ cars }) => {
  return (
    <div className="bg-white border border-neutral-200 rounded-lg overflow-hidden">
      <table className="w-full">
        <thead className="bg-neutral-50 border-b border-neutral-200">
          <tr>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Image</th>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Car Details</th>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Type</th>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Location</th>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Price/Day</th>
            <th className="px-6 py-4 text-left text-sm font-semibold text-neutral-700">Action</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-neutral-200">
          {cars.map((car) => (
            <tr key={car.id} className="hover:bg-neutral-50">
              <td className="px-6 py-4">
                {car.image ? (
                  <img
                    src={car.image}
                    alt={car.name}
                    className="h-20 w-32 object-cover rounded"
                  />
                ) : (
                  <div className="h-20 w-32 bg-neutral-200 rounded" />
                )}
              </td>
              <td className="px-6 py-4">
                <div>
                  <div className="font-semibold text-neutral-900">{car.name}</div>
                  <div className="text-sm text-neutral-600">
                    {car.brand} {car.model}
                  </div>
                  <div className="text-xs text-neutral-500 mt-1">
                    {car.seats} seats • {car.transmission} • {car.fuelType}
                  </div>
                </div>
              </td>
              <td className="px-6 py-4">
                <span className="text-sm text-neutral-700">{car.type}</span>
              </td>
              <td className="px-6 py-4 text-neutral-600">{car.location}</td>
              <td className="px-6 py-4">
                <span className="font-semibold text-neutral-900">
                  {formatPricePerDay(car.pricePerDay)}
                </span>
              </td>
              <td className="px-6 py-4">
                <Link
                  to={`/cars/${car.id}`}
                  className="px-4 py-2 bg-primary-600 text-white rounded text-sm font-medium hover:bg-primary-700 transition-colors inline-block"
                >
                  View Details
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CarTable;
