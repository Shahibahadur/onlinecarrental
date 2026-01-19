import React, { useState } from 'react';
import { useSelector } from 'react-redux';
import { Car, Users, Wallet, TrendingUp } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import type { RootState } from '../store';
import { adminAPI } from '../api/admin';
import VehicleManagement from './Admin/VehicleManagement';
import BookingManagement from './Admin/BookingManagement';
import FeedbackManagement from './Admin/FeedbackManagement';
import UserManagement from './Admin/UserManagement';

const Admin: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);
  const [activeTab, setActiveTab] = useState('overview');

  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: ['adminStats'],
    queryFn: async () => {
      const response = await adminAPI.getStats();
      return response.data;
    },
  });

  const stats = [
    { 
      name: 'Total Cars', 
      value: statsData?.totalCars?.toString() || '0', 
      icon: Car, 
      change: '+12%', 
      changeType: 'increase' as const 
    },
    { 
      name: 'Total Users', 
      value: statsData?.totalUsers?.toString() || '0', 
      icon: Users, 
      change: '+8%', 
      changeType: 'increase' as const 
    },
    { 
      name: 'Revenue', 
      value: `रू ${statsData?.revenue?.toLocaleString('en-NP') || '0'}`, 
      icon: Wallet, 
      change: '+23%', 
      changeType: 'increase' as const 
    },
    { 
      name: 'Bookings', 
      value: statsData?.totalBookings?.toString() || '0', 
      icon: TrendingUp, 
      change: '+5%', 
      changeType: 'increase' as const 
    },
  ];

  if (!user || user.email !== 'admin@rental.com') {
    return (
      <div className="min-h-screen bg-neutral-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-lg text-neutral-600">Access denied. Admin privileges required.</div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-neutral-900">Admin Dashboard</h1>
          <p className="text-neutral-600 mt-2">Manage your car rental platform</p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {stats.map((stat) => (
            <div key={stat.name} className="bg-white p-6 rounded-lg shadow-sm border border-neutral-200">
              <div className="flex items-center">
                <div className="bg-primary-100 p-3 rounded-full">
                  <stat.icon className="h-6 w-6 text-primary-600" />
                </div>
                <div className="ml-4 flex-1">
                  <p className="text-sm font-medium text-neutral-600">{stat.name}</p>
                  <div className="flex items-center justify-between">
                    <p className="text-2xl font-bold text-neutral-900">{stat.value}</p>
                    <p className={`text-sm font-medium ${
                      stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {stat.change}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-sm border border-neutral-200 mb-8">
          <div className="border-b border-neutral-200">
            <nav className="flex -mb-px">
              <button
                onClick={() => setActiveTab('overview')}
                className={`py-4 px-6 text-sm font-medium border-b-2 ${
                  activeTab === 'overview'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'
                }`}
              >
                Overview
              </button>
              <button
                onClick={() => setActiveTab('vehicles')}
                className={`py-4 px-6 text-sm font-medium border-b-2 ${
                  activeTab === 'vehicles'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'
                }`}
              >
                Vehicle Management
              </button>
              <button
                onClick={() => setActiveTab('bookings')}
                className={`py-4 px-6 text-sm font-medium border-b-2 ${
                  activeTab === 'bookings'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'
                }`}
              >
                Booking Management
              </button>
              <button
                onClick={() => setActiveTab('feedback')}
                className={`py-4 px-6 text-sm font-medium border-b-2 ${
                  activeTab === 'feedback'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'
                }`}
              >
                Feedback
              </button>
              <button
                onClick={() => setActiveTab('users')}
                className={`py-4 px-6 text-sm font-medium border-b-2 ${
                  activeTab === 'users'
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-neutral-500 hover:text-neutral-700 hover:border-neutral-300'
                }`}
              >
                Users
              </button>
            </nav>
          </div>

          <div className="p-6">
            {activeTab === 'overview' && (
              <div>
                <h3 className="text-lg font-semibold text-neutral-900 mb-6">Admin Overview</h3>
                {statsLoading ? (
                  <div className="text-center py-8">
                    <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {stats.map((stat) => (
                      <div key={stat.name} className="bg-neutral-50 p-4 rounded-lg border border-neutral-200">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm font-medium text-neutral-600">{stat.name}</p>
                            <p className="text-2xl font-bold text-neutral-900 mt-1">{stat.value}</p>
                          </div>
                          <div className="bg-primary-100 p-3 rounded-full">
                            <stat.icon className="h-6 w-6 text-primary-600" />
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'vehicles' && <VehicleManagement />}

            {activeTab === 'bookings' && <BookingManagement />}

            {activeTab === 'feedback' && <FeedbackManagement />}

            {activeTab === 'users' && <UserManagement />}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Admin;