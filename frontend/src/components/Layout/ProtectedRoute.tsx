import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import type { RootState } from '../../store';

interface ProtectedRouteProps {
  children: React.ReactNode;
  adminOnly?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, adminOnly = false }) => {
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
  const location = useLocation();

  if (!isAuthenticated) {
    // Remember where the user wanted to go so Login can send them back
    return <Navigate to="/login" replace state={{ returnTo: location.pathname }} />;
  }

  if (adminOnly) {
    const role = user?.role?.toUpperCase();
    const isLegacyAdminEmail = user?.email === 'admin@rental.com';

    // Allow access if the backend role is ADMIN or if it's the legacy hard-coded admin email
    if (role !== 'ADMIN' && !isLegacyAdminEmail) {
      return <Navigate to="/" replace />;
    }
  }

  return <>{children}</>;
};

export default ProtectedRoute;
