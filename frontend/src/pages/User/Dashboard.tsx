import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Calendar, MapPin, Car, Clock, X, CheckCircle, AlertCircle, CreditCard, RotateCcw } from 'lucide-react';
import { bookingAPI } from '../../api/booking';
import { formatCurrency } from '../../constants/locale';
import type { RootState } from '../../store';
import { Link } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const [activeTab, setActiveTab] = useState<'all' | 'pending' | 'confirmed' | 'active' | 'completed' | 'cancelled'>('all');
  const queryClient = useQueryClient();

  const { data: bookings, isLoading, error } = useQuery({
    queryKey: ['userBookings'],
    queryFn: async () => {
      const response = await bookingAPI.getByUser();
      return response.data;
    },
    enabled: !!user,
  });

  const returnCarMutation = useMutation({
    mutationFn: (bookingId: string) => bookingAPI.returnCar(bookingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userBookings'] });
      alert('Car returned successfully!');
    },
    onError: (error: any) => {
      alert(error?.response?.data?.message || 'Failed to return car. Please try again.');
    },
  });

  const filteredBookings = bookings?.filter((booking: any) => {
    if (activeTab === 'all') return true;
    return booking.status?.toLowerCase() === activeTab;
  }) || [];

  const stats = {
    total: bookings?.length || 0,
    completed: bookings?.filter((b: any) => b.status?.toLowerCase() === 'completed').length || 0,
    pending: bookings?.filter((b: any) => b.status?.toLowerCase() === 'pending').length || 0,
    totalSpent: bookings?.filter((b: any) => b.status?.toLowerCase() === 'completed')
      .reduce((sum: number, b: any) => sum + (b.totalPrice || 0), 0) || 0,
  };

  const getStatusBadge = (status: string) => {
    const statusLower = status?.toLowerCase() || '';
    const badges: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
      pending: { color: 'bg-yellow-100 text-yellow-800', icon: <Clock className="h-4 w-4" />, label: 'Pending' },
      confirmed: { color: 'bg-blue-100 text-blue-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Confirmed' },
      active: { color: 'bg-purple-100 text-purple-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Active' },
      completed: { color: 'bg-green-100 text-green-800', icon: <CheckCircle className="h-4 w-4" />, label: 'Completed' },
      cancelled: { color: 'bg-red-100 text-red-800', icon: <X className="h-4 w-4" />, label: 'Cancelled' },
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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-neutral-900 mb-2">My Dashboard</h1>
        <p className="text-neutral-600">Welcome back, {user?.firstName}!</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-primary-600">
          <h3 className="text-neutral-600 text-sm font-medium mb-2">Total Bookings</h3>
          <p className="text-3xl font-bold text-neutral-900">{stats.total}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-yellow-500">
          <h3 className="text-neutral-600 text-sm font-medium mb-2">Pending</h3>
          <p className="text-3xl font-bold text-neutral-900">{stats.pending}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-green-500">
          <h3 className="text-neutral-600 text-sm font-medium mb-2">Completed Rides</h3>
          <p className="text-3xl font-bold text-neutral-900">{stats.completed}</p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md border-l-4 border-blue-500">
          <h3 className="text-neutral-600 text-sm font-medium mb-2">Total Spent</h3>
          <p className="text-3xl font-bold text-neutral-900">{formatCurrency(stats.totalSpent)}</p>
        </div>
      </div>

      {/* Bookings Section */}
      <div className="bg-white rounded-lg shadow-md">
        <div className="p-6 border-b border-neutral-200">
          <h2 className="text-xl font-bold text-neutral-900 mb-4">My Bookings</h2>

          {/* Tabs */}
          <div className="flex space-x-2 overflow-x-auto">
            {[
              { key: 'all', label: 'All' },
              { key: 'pending', label: 'Pending' },
              { key: 'confirmed', label: 'Confirmed' },
              { key: 'active', label: 'Active' },
              { key: 'completed', label: 'Completed' },
              { key: 'cancelled', label: 'Cancelled' },
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key as any)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors whitespace-nowrap ${
                  activeTab === tab.key
                    ? 'bg-primary-600 text-white'
                    : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        <div className="p-6">
          {isLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
              <p className="mt-4 text-neutral-600">Loading bookings...</p>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
              <p className="text-neutral-600">Failed to load bookings. Please try again.</p>
            </div>
          ) : filteredBookings.length === 0 ? (
            <div className="text-center py-12">
              <Car className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-neutral-900 mb-2">No bookings found</h3>
              <p className="text-neutral-600 mb-6">
                {activeTab === 'all'
                  ? "You haven't made any bookings yet. Start exploring our vehicles!"
                  : `No ${activeTab} bookings found.`}
              </p>
              <Link
                to="/cars"
                className="inline-block px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                Browse Cars
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredBookings.map((booking: any) => (
                <div
                  key={booking.id}
                  className="border border-neutral-200 rounded-lg p-6 hover:shadow-md transition-shadow"
                >
                  <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-start justify-between mb-4">
                        <div>
                          <h3 className="text-lg font-semibold text-neutral-900 mb-1">
                            {booking.vehicle?.name || booking.carName || 'Car Rental'}
                          </h3>
                          <p className="text-sm text-neutral-600">
                            Booking ID: #{booking.id}
                          </p>
                        </div>
                        {getStatusBadge(booking.status)}
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                        <div className="flex items-center text-neutral-600">
                          <Calendar className="h-4 w-4 mr-2" />
                          <span className="text-sm">
                            {formatDate(booking.startDate)} - {formatDate(booking.endDate)}
                          </span>
                        </div>
                        <div className="flex items-center text-neutral-600">
                          <MapPin className="h-4 w-4 mr-2" />
                          <span className="text-sm">
                            {booking.pickupLocation} → {booking.dropoffLocation}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center text-neutral-900">
                        <CreditCard className="h-4 w-4 mr-2 text-neutral-600" />
                        <span className="text-lg font-bold">{formatCurrency(booking.totalPrice || 0)}</span>
                      </div>
                    </div>

                    <div className="flex flex-col gap-2">
                      {booking.status?.toLowerCase() === 'pending' && (
                        <button
                          onClick={async () => {
                            if (confirm('Are you sure you want to cancel this booking?')) {
                              try {
                                await bookingAPI.cancel(booking.id);
                                queryClient.invalidateQueries({ queryKey: ['userBookings'] });
                              } catch (error) {
                                alert('Failed to cancel booking');
                              }
                            }
                          }}
                          className="px-4 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition-colors text-sm font-medium"
                        >
                          Cancel Booking
                        </button>
                      )}
                      {(booking.status?.toLowerCase() === 'active' || booking.status?.toLowerCase() === 'confirmed') && (
                        <button
                          onClick={() => {
                            if (confirm('Are you sure you want to return this car? This will mark the booking as completed.')) {
                              returnCarMutation.mutate(booking.id);
                            }
                          }}
                          disabled={returnCarMutation.isPending}
                          className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          <RotateCcw className="h-4 w-4" />
                          {returnCarMutation.isPending ? 'Returning...' : 'Return Car'}
                        </button>
                      )}
                      <Link
                        to={`/cars/${booking.vehicle?.id || booking.carId}`}
                        className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm font-medium text-center"
                      >
                        View Details
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

