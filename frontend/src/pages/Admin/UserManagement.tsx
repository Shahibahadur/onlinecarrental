import React from 'react';

const UserManagement: React.FC = () => {
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-bold text-neutral-900 mb-4">User Management</h2>
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-neutral-50">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-medium text-neutral-900">ID</th>
              <th className="px-6 py-3 text-left text-sm font-medium text-neutral-900">Name</th>
              <th className="px-6 py-3 text-left text-sm font-medium text-neutral-900">Email</th>
              <th className="px-6 py-3 text-left text-sm font-medium text-neutral-900">Phone</th>
              <th className="px-6 py-3 text-left text-sm font-medium text-neutral-900">Actions</th>
            </tr>
          </thead>
          <tbody className="border-t border-neutral-200">
            <tr className="text-center py-8 text-neutral-600">
              <td colSpan={5}>No users found</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserManagement;
