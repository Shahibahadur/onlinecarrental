import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Calendar, MapPin, DollarSign, User, Car, Filter, Search, CheckCircle, XCircle, Clock, Eye, RotateCcw } from 'lucide-react';
import { adminAPI } from '../../api/admin';
import { formatCurrency } from '../../constants/locale';
import type { Booking } from '../../types/api';

const BookingManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');

  const { data, isLoading, error } = useQuery({
    queryKey: ['adminBookings', page, pageSize, statusFilter],
    queryFn: async () => {
      const response = await adminAPI.getAllBookings(page, pageSize);
      return response.data;
    },
  });

  const statusUpdateMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      adminAPI.updateBookingStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBookings'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
  });

  const returnCarMutation = useMutation({
    mutationFn: (id: string) => adminAPI.returnCar(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBookings'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
      alert('Car returned successfully!');
    },
    onError: (error: any) => {
      alert(error?.response?.data?.message || 'Failed to return car. Please try again.');
    },
  });

  const bookings = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const totalElements = data?.totalElements || 0;

  const filteredBookings = bookings.filter((booking: any) => {
    const matchesStatus = statusFilter === 'all' || booking.status?.toLowerCase() === statusFilter.toLowerCase();
    const matchesSearch = !searchTerm || 
      booking.id?.toString().includes(searchTerm) ||
      booking.user?.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      booking.vehicle?.name?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesStatus && matchesSearch;
  });

  const getStatusBadge = (status: string) => {
    const statusLower = status?.toLowerCase() || '';
    const badges: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
      pending: { color: 'bg-yellow-100 text-yellow-800', icon: <Clock className="h-4 w-4" />, label: 'Pending' },
      confirmed: { color: 'bg-blue-100 text-blue-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Confirmed' },
      active: { color: 'bg-purple-100 text-purple-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Active' },
      completed: { color: 'bg-green-100 text-green-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Completed' },
      cancelled: { color: 'bg-red-100 text-red-800', icon: <XCircle className="h-4 w-4" />, label: 'Cancelled' },
    };
    const badge = badges[statusLower] || badges.pending;
    return (
      <span className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium ${badge.color}`}>
        {badge.icon}
        {badge.label}
      </span>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const handleStatusUpdate = (bookingId: string, newStatus: string) => {
    if (window.confirm(`Are you sure you want to change booking status to ${newStatus}?`)) {
      statusUpdateMutation.mutate({ id: bookingId, status: newStatus });
    }
  };

  const handleReturnCar = (bookingId: string) => {
    if (window.confirm('Are you sure you want to return this car? This will mark the vehicle as available and the booking as completed.')) {
      returnCarMutation.mutate(bookingId);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-neutral-900">Booking Management</h2>
          <p className="text-neutral-600">View and manage all bookings</p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-md p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-neutral-400" />
            <input
              type="text"
              placeholder="Search by booking ID, user email, or vehicle name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div className="flex items-center space-x-2">
            <Filter className="h-5 w-5 text-neutral-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="all">All Status</option>
                            <option value="pending">Pending</option>
                            <option value="confirmed">Confirmed</option>
                            <option value="active">Active</option>
                            <option value="completed">Completed</option>
                            <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Bookings Table */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-neutral-600">Loading bookings...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">
            Failed to load bookings. Please try again.
          </div>
        ) : filteredBookings.length === 0 ? (
          <div className="p-8 text-center text-neutral-600">
            No bookings found.
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-neutral-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Booking ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Customer
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Vehicle
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Dates
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Location
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Amount
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
                  {filteredBookings.map((booking: any) => (
                    <tr key={booking.id} className="hover:bg-neutral-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-neutral-900">
                        #{booking.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <User className="h-4 w-4 text-neutral-400 mr-2" />
                          <div>
                            <div className="text-sm font-medium text-neutral-900">
                              {booking.user?.firstName} {booking.user?.lastName}
                            </div>
                            <div className="text-sm text-neutral-500">{booking.user?.email}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <Car className="h-4 w-4 text-neutral-400 mr-2" />
                          <div className="text-sm text-neutral-900">
                            {booking.vehicle?.name || booking.carName || 'N/A'}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-neutral-900">
                          <div className="flex items-center">
                            <Calendar className="h-4 w-4 mr-1 text-neutral-400" />
                            {formatDate(booking.startDate)}
                          </div>
                          <div className="text-neutral-500 text-xs mt-1">to {formatDate(booking.endDate)}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-neutral-600">
                          <div className="flex items-center">
                            <MapPin className="h-4 w-4 mr-1 text-neutral-400" />
                            {booking.pickupLocation}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <DollarSign className="h-4 w-4 text-green-600 mr-1" />
                          <span className="text-sm font-medium text-neutral-900">
                            {formatCurrency(booking.totalPrice || 0)}
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(booking.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex items-center space-x-2">
                          <select
                            value={booking.status || 'pending'}
                            onChange={(e) => handleStatusUpdate(booking.id, e.target.value)}
                            className="text-xs px-2 py-1 border border-neutral-300 rounded focus:outline-none focus:ring-2 focus:ring-primary-500"
                            disabled={statusUpdateMutation.isPending}
                          >
                            <option value="pending">Pending</option>
                            <option value="confirmed">Confirmed</option>
                            <option value="active">Active</option>
                            <option value="completed">Completed</option>
                            <option value="cancelled">Cancelled</option>
                          </select>
                          {(booking.status?.toLowerCase() === 'active' || booking.status?.toLowerCase() === 'confirmed') && (
                            <button
                              onClick={() => handleReturnCar(booking.id)}
                              disabled={returnCarMutation.isPending}
                              className="px-3 py-1 bg-green-600 text-white rounded text-xs font-medium hover:bg-green-700 transition-colors flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
                              title="Return Car"
                            >
                              <RotateCcw className="h-3 w-3" />
                              Return
                            </button>
                          )}
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
                  Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} bookings
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
    </div>
  );
};

export default BookingManagement;
