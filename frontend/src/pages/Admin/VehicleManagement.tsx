import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit, Trash2, Eye, CheckCircle, XCircle, Search, Filter, X } from 'lucide-react';
import { adminAPI } from '../../api/admin';
import { formatPricePerDay } from '../../constants/locale';
import type { Car } from '../../types/car';
import CarForm from '../../components/Car/CarForm';
import CarDetailModal from '../../components/Car/CarDetailModal';

const VehicleManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingCar, setEditingCar] = useState<Car | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['adminVehicles', page, pageSize],
    queryFn: async () => {
      const response = await adminAPI.getAllVehicles(page, pageSize);
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminAPI.deleteVehicle(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicles'] });
      queryClient.invalidateQueries({ queryKey: ['cars'] });
    },
  });

  const availabilityMutation = useMutation({
    mutationFn: ({ id, available }: { id: string; available: boolean }) =>
      adminAPI.updateVehicleAvailability(id, available),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicles'] });
      queryClient.invalidateQueries({ queryKey: ['cars'] });
    },
  });

  const vehicles = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  const handleEdit = (car: Car) => {
    setEditingCar(car);
    setIsFormOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this vehicle?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleToggleAvailability = (car: Car) => {
    availabilityMutation.mutate({
      id: car.id,
      available: !car.available,
    });
  };

  const filteredVehicles = vehicles.filter((car) =>
    car.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    car.brand.toLowerCase().includes(searchTerm.toLowerCase()) ||
    car.location.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-neutral-900">Vehicle Management</h2>
          <p className="text-neutral-600">Manage your vehicle fleet</p>
        </div>
        <button
          onClick={() => {
            setEditingCar(null);
            setIsFormOpen(true);
          }}
          className="inline-flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          <Plus className="h-5 w-5 mr-2" />
          Add New Vehicle
        </button>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow-md p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-neutral-400" />
            <input
              type="text"
              placeholder="Search vehicles by name, brand, or location..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>
      </div>

      {/* Vehicles Table */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-neutral-600">Loading vehicles...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">
            Failed to load vehicles. Please try again.
          </div>
        ) : filteredVehicles.length === 0 ? (
          <div className="p-8 text-center text-neutral-600">
            No vehicles found. Add your first vehicle to get started.
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-neutral-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Vehicle
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Location
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Price/Day
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-neutral-200">
                  {filteredVehicles.map((car) => (
                    <tr key={car.id} className="hover:bg-neutral-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <img
                            src={car.image}
                             alt={car.name}
                             className="h-12 w-12 rounded-lg object-cover mr-3 bg-neutral-200"
                             onError={(e) => {
                               e.currentTarget.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"%3E%3Cpath stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /%3E%3C/svg%3E';
                             }}
                          />
                          <div>
                            <div className="text-sm font-medium text-neutral-900">{car.name}</div>
                            <div className="text-sm text-neutral-500">{car.brand} {car.model}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="px-2 py-1 text-xs font-medium rounded-full bg-primary-100 text-primary-800">
                          {car.type}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                        {car.location}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-neutral-900">
                        {formatPricePerDay(car.pricePerDay)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <button
                          onClick={() => handleToggleAvailability(car)}
                          className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${
                            car.available
                              ? 'bg-green-100 text-green-800'
                              : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {car.available ? (
                            <>
                              <CheckCircle className="h-3 w-3 mr-1" />
                              Available
                            </>
                          ) : (
                            <>
                              <XCircle className="h-3 w-3 mr-1" />
                              Unavailable
                            </>
                          )}
                        </button>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => setSelectedCar(car)}
                            className="text-primary-600 hover:text-primary-900"
                            title="View Details"
                          >
                            <Eye className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleEdit(car)}
                            className="text-blue-600 hover:text-blue-900"
                            title="Edit"
                          >
                            <Edit className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(car.id)}
                            className="text-red-600 hover:text-red-900"
                            title="Delete"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="bg-neutral-50 px-6 py-4 flex items-center justify-between border-t border-neutral-200">
                <div className="text-sm text-neutral-600">
                  Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} vehicles
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="px-4 py-2 border border-neutral-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-neutral-50"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="px-4 py-2 border border-neutral-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-neutral-50"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Car Form Modal */}
      {isFormOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen px-4">
            <div className="fixed inset-0 bg-black bg-opacity-50" onClick={() => setIsFormOpen(false)} />
            <div className="relative bg-white rounded-lg max-w-2xl w-full p-6 max-h-[90vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-bold text-neutral-900">
                  {editingCar ? 'Edit Vehicle' : 'Add New Vehicle'}
                </h2>
                <button
                  onClick={() => {
                    setIsFormOpen(false);
                    setEditingCar(null);
                  }}
                  className="text-neutral-600 hover:text-neutral-900"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>
              <CarForm
                car={editingCar || undefined}
                onSuccess={() => {
                  setIsFormOpen(false);
                  setEditingCar(null);
                  queryClient.invalidateQueries({ queryKey: ['adminVehicles'] });
                }}
                onCancel={() => {
                  setIsFormOpen(false);
                  setEditingCar(null);
                }}
              />
            </div>
          </div>
        </div>
      )}

      {/* Car Detail Modal */}
      {selectedCar && (
        <CarDetailModal
          car={selectedCar}
          isOpen={!!selectedCar}
          onClose={() => setSelectedCar(null)}
          onBook={() => {}}
        />
      )}
    </div>
  );
};

export default VehicleManagement;
