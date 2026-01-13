import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Users, Car, Calendar, DollarSign, TrendingUp, AlertCircle, CheckCircle, Clock, X } from 'lucide-react';
import { adminAPI } from '../../api/admin';
import { formatCurrency } from '../../constants/locale';
import BookingManagement from './BookingManagement';
import UserManagement from './UserManagement';
import VehicleManagement from './VehicleManagement';

const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'vehicles' | 'bookings' | 'users'>('overview');

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['adminStats'],
    queryFn: async () => {
      const response = await adminAPI.getStats();
      return response.data;
    },
  });

  const statsData = stats || {
    totalUsers: 0,
    totalCars: 0,
    totalBookings: 0,
    revenue: 0,
    activeBookings: 0,
    pendingBookings: 0,
    completedBookings: 0,
  };

  const tabs = [
    { id: 'overview', label: 'Overview', icon: TrendingUp },
    { id: 'vehicles', label: 'Vehicles', icon: Car },
    { id: 'bookings', label: 'Bookings', icon: Calendar },
    { id: 'users', label: 'Users', icon: Users },
  ];

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900 mb-2">Admin Dashboard</h1>
          <p className="text-neutral-600">Manage your car rental system</p>
        </div>

        {/* Tabs */}
        <div className="flex gap-2 mb-8 border-b border-neutral-200 overflow-x-auto">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`px-6 py-3 font-medium border-b-2 transition-colors flex items-center space-x-2 whitespace-nowrap ${
                  activeTab === tab.id
                    ? 'border-primary-600 text-primary-600'
                    : 'border-transparent text-neutral-600 hover:text-neutral-900'
                }`}
              >
                <Icon className="h-5 w-5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Overview Tab */}
        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Statistics Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-neutral-600">Total Users</p>
                    <p className="text-3xl font-bold text-neutral-900 mt-2">
                      {statsLoading ? '...' : statsData.totalUsers}
                    </p>
                  </div>
                  <div className="bg-blue-100 p-3 rounded-full">
                    <Users className="h-6 w-6 text-blue-600" />
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow-md p-6 border-l-4 border-green-500">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-neutral-600">Total Vehicles</p>
                    <p className="text-3xl font-bold text-neutral-900 mt-2">
                      {statsLoading ? '...' : statsData.totalCars}
                    </p>
                  </div>
                  <div className="bg-green-100 p-3 rounded-full">
                    <Car className="h-6 w-6 text-green-600" />
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow-md p-6 border-l-4 border-purple-500">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-neutral-600">Total Bookings</p>
                    <p className="text-3xl font-bold text-neutral-900 mt-2">
                      {statsLoading ? '...' : statsData.totalBookings}
                    </p>
                  </div>
                  <div className="bg-purple-100 p-3 rounded-full">
                    <Calendar className="h-6 w-6 text-purple-600" />
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow-md p-6 border-l-4 border-yellow-500">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-neutral-600">Total Revenue</p>
                    <p className="text-3xl font-bold text-neutral-900 mt-2">
                      {statsLoading ? '...' : formatCurrency(statsData.revenue)}
                    </p>
                  </div>
                  <div className="bg-yellow-100 p-3 rounded-full">
                    <DollarSign className="h-6 w-6 text-yellow-600" />
                  </div>
                </div>
              </div>
            </div>

            {/* Booking Status Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-neutral-900">Active Bookings</h3>
                  <div className="bg-blue-100 p-2 rounded-full">
                    <Clock className="h-5 w-5 text-blue-600" />
                  </div>
                </div>
                <p className="text-3xl font-bold text-blue-600">{statsData.activeBookings}</p>
                <p className="text-sm text-neutral-600 mt-2">Currently active rentals</p>
              </div>

              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-neutral-900">Pending Bookings</h3>
                  <div className="bg-yellow-100 p-2 rounded-full">
                    <AlertCircle className="h-5 w-5 text-yellow-600" />
                  </div>
                </div>
                <p className="text-3xl font-bold text-yellow-600">{statsData.pendingBookings || 0}</p>
                <p className="text-sm text-neutral-600 mt-2">Awaiting confirmation</p>
              </div>

              <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-neutral-900">Completed</h3>
                  <div className="bg-green-100 p-2 rounded-full">
                    <CheckCircle className="h-5 w-5 text-green-600" />
                  </div>
                </div>
                <p className="text-3xl font-bold text-green-600">{statsData.completedBookings || 0}</p>
                <p className="text-sm text-neutral-600 mt-2">Successfully completed</p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-bold text-neutral-900 mb-4">Quick Actions</h2>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <button
                  onClick={() => setActiveTab('vehicles')}
                  className="p-4 border-2 border-neutral-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors text-left"
                >
                  <Car className="h-6 w-6 text-primary-600 mb-2" />
                  <h3 className="font-semibold text-neutral-900">Add New Vehicle</h3>
                  <p className="text-sm text-neutral-600">Register a new car to the fleet</p>
                </button>

                <button
                  onClick={() => setActiveTab('bookings')}
                  className="p-4 border-2 border-neutral-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors text-left"
                >
                  <Calendar className="h-6 w-6 text-primary-600 mb-2" />
                  <h3 className="font-semibold text-neutral-900">Manage Bookings</h3>
                  <p className="text-sm text-neutral-600">View and manage all bookings</p>
                </button>

                <button
                  onClick={() => setActiveTab('users')}
                  className="p-4 border-2 border-neutral-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-colors text-left"
                >
                  <Users className="h-6 w-6 text-primary-600 mb-2" />
                  <h3 className="font-semibold text-neutral-900">Manage Users</h3>
                  <p className="text-sm text-neutral-600">View and manage user accounts</p>
                </button>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-bold text-neutral-900 mb-4">Recent Activity</h2>
              <div className="space-y-4">
                <div className="flex items-center space-x-4 p-4 bg-neutral-50 rounded-lg">
                  <div className="bg-green-100 p-2 rounded-full">
                    <CheckCircle className="h-5 w-5 text-green-600" />
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-neutral-900">New booking received</p>
                    <p className="text-sm text-neutral-600">2 minutes ago</p>
                  </div>
                </div>
                <div className="flex items-center space-x-4 p-4 bg-neutral-50 rounded-lg">
                  <div className="bg-blue-100 p-2 rounded-full">
                    <Users className="h-5 w-5 text-blue-600" />
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-neutral-900">New user registered</p>
                    <p className="text-sm text-neutral-600">15 minutes ago</p>
                  </div>
                </div>
                <div className="flex items-center space-x-4 p-4 bg-neutral-50 rounded-lg">
                  <div className="bg-purple-100 p-2 rounded-full">
                    <Car className="h-5 w-5 text-purple-600" />
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-neutral-900">Vehicle added to fleet</p>
                    <p className="text-sm text-neutral-600">1 hour ago</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Vehicles Tab */}
        {activeTab === 'vehicles' && <VehicleManagement />}

        {/* Bookings Tab */}
        {activeTab === 'bookings' && <BookingManagement />}

        {/* Users Tab */}
        {activeTab === 'users' && <UserManagement />}
      </div>
    </div>
  );
};

export default AdminDashboard;
