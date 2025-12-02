import React from 'react';

const Dashboard: React.FC = () => {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-neutral-900 mb-8">My Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-neutral-600 text-sm font-medium">Total Bookings</h3>
          <p className="text-3xl font-bold text-neutral-900 mt-2">0</p>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-neutral-600 text-sm font-medium">Completed Rides</h3>
          <p className="text-3xl font-bold text-neutral-900 mt-2">0</p>
        </div>
        
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-neutral-600 text-sm font-medium">Total Spent</h3>
          <p className="text-3xl font-bold text-neutral-900 mt-2">$0</p>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-xl font-bold text-neutral-900 mb-4">Recent Bookings</h2>
        <p className="text-neutral-600">No bookings yet</p>
      </div>
    </div>
  );
};

export default Dashboard;
