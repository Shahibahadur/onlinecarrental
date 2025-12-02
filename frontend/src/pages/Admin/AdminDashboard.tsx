import React from 'react';
import BookingManagement from './BookingManagement';
import UserManagement from './UserManagement';

const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState<'bookings' | 'users'>('bookings');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-neutral-900 mb-8">Admin Dashboard</h1>

      <div className="flex gap-4 mb-8 border-b border-neutral-200">
        <button
          onClick={() => setActiveTab('bookings')}
          className={`px-4 py-2 font-medium border-b-2 transition-colors ${
            activeTab === 'bookings'
              ? 'border-primary-600 text-primary-600'
              : 'border-transparent text-neutral-600 hover:text-neutral-900'
          }`}
        >
          Bookings
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={`px-4 py-2 font-medium border-b-2 transition-colors ${
            activeTab === 'users'
              ? 'border-primary-600 text-primary-600'
              : 'border-transparent text-neutral-600 hover:text-neutral-900'
          }`}
        >
          Users
        </button>
      </div>

      <div className="mt-6">
        {activeTab === 'bookings' && <BookingManagement />}
        {activeTab === 'users' && <UserManagement />}
      </div>
    </div>
  );
};

export default AdminDashboard;
