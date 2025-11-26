import React from 'react';
import { useSelector } from 'react-redux';
import { Calendar, Car, MapPin, DollarSign, Clock } from 'lucide-react';
import { RootState } from '../store';
import { Booking } from '../types';

const Dashboard: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);

  // Mock bookings data
  const mockBookings: Booking[] = [
    {
      id: '1',
      carId: '1',
      userId: '1',
      startDate: '2024-12-01',
      endDate: '2024-12-05',
      totalPrice: 225,
      status: 'confirmed',
      pickupLocation: 'New York',
      dropoffLocation: 'New York',
      createdAt: '2024-11-25',
    },
    {
      id: '2',
      carId: '3',
      userId: '1',
      startDate: '2024-12-10',
      endDate: '2024-12-12',
      totalPrice: 150,
      status: 'pending',
      pickupLocation: 'San Francisco',
      dropoffLocation: 'San Francisco',
      createdAt: '2024-11-26',
    },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'confirmed':
        return 'bg-green-100 text-green-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      case 'completed':
        return 'bg-blue-100 text-blue-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-lg text-neutral-600">Please log in to view your dashboard</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900">Dashboard</h1>
          <p className="text-neutral-600 mt-2">
            Welcome back, {user.firstName} {user.lastName}
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200">
            <div className="flex items-center">
              <div className="bg-primary-100 p-3 rounded-full">
                <Car className="h-6 w-6 text-primary-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-neutral-600">Total Bookings</p>
                <p className="text-2xl font-bold text-neutral-900">{mockBookings.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200">
            <div className="flex items-center">
              <div className="bg-green-100 p-3 rounded-full">
                <DollarSign className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-neutral-600">Total Spent</p>
                <p className="text-2xl font-bold text-neutral-900">
                  ${mockBookings.reduce((sum, booking) => sum + booking.totalPrice, 0)}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200">
            <div className="flex items-center">
              <div className="bg-blue-100 p-3 rounded-full">
                <Clock className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-neutral-600">Upcoming Trips</p>
                <p className="text-2xl font-bold text-neutral-900">
                  {mockBookings.filter(b => b.status === 'confirmed' || b.status === 'pending').length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Bookings Section */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200">
          <div className="px-6 py-4 border-b border-neutral-200">
            <h2 className="text-xl font-semibold text-neutral-900">Your Bookings</h2>
          </div>

          <div className="divide-y divide-neutral-200">
            {mockBookings.length > 0 ? (
              mockBookings.map((booking) => (
                <div key={booking.id} className="p-6">
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-4 mb-3">
                        <Car className="h-5 w-5 text-neutral-400" />
                        <h3 className="text-lg font-medium text-neutral-900">
                          Booking #{booking.id}
                        </h3>
                        <span className={`px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(booking.status)}`}>
                          {booking.status.charAt(0).toUpperCase() + booking.status.slice(1)}
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-neutral-600">
                        <div className="flex items-center">
                          <Calendar className="h-4 w-4 mr-2" />
                          <span>
                            {new Date(booking.startDate).toLocaleDateString()} - {new Date(booking.endDate).toLocaleDateString()}
                          </span>
                        </div>
                        <div className="flex items-center">
                          <MapPin className="h-4 w-4 mr-2" />
                          <span>{booking.pickupLocation}</span>
                        </div>
                        <div className="flex items-center">
                          <DollarSign className="h-4 w-4 mr-2" />
                          <span className="font-semibold">${booking.totalPrice}</span>
                        </div>
                      </div>
                    </div>

                    <div className="mt-4 lg:mt-0 lg:ml-6">
                      <button className="px-4 py-2 text-sm font-medium text-primary-600 bg-primary-50 rounded-md hover:bg-primary-100 transition-colors">
                        View Details
                      </button>
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center">
                <Car className="h-12 w-12 text-neutral-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-neutral-900 mb-2">No bookings yet</h3>
                <p className="text-neutral-600 mb-4">Start your journey by booking your first car.</p>
                <button className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors">
                  Browse Cars
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Profile Info */}
        <div className="mt-8 bg-white rounded-lg shadow-sm border border-neutral-200">
          <div className="px-6 py-4 border-b border-neutral-200">
            <h2 className="text-xl font-semibold text-neutral-900">Profile Information</h2>
          </div>
          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Full Name
                </label>
                <p className="text-neutral-900">
                  {user.firstName} {user.lastName}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Email
                </label>
                <p className="text-neutral-900">{user.email}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Phone
                </label>
                <p className="text-neutral-900">{user.phone}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-neutral-700 mb-1">
                  Member Since
                </label>
                <p className="text-neutral-900">November 2024</p>
              </div>
            </div>
            <div className="mt-6">
              <button className="px-4 py-2 text-sm font-medium text-primary-600 bg-primary-50 rounded-md hover:bg-primary-100 transition-colors">
                Edit Profile
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;