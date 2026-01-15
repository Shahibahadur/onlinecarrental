import React, { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { setUser, setLoading } from '../../store/auth.store';
import type { AppDispatch } from '../../store';

interface AuthProviderProps {
  children: React.ReactNode;
}

const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const dispatch = useDispatch<AppDispatch>();

  useEffect(() => {
    const initializeAuth = () => {
      dispatch(setLoading(true));
      
      try {
        // Check for stored user data
        const storedUser = localStorage.getItem('user');
        const authToken = localStorage.getItem('authToken');
        
        if (storedUser && authToken) {
          const user = JSON.parse(storedUser);
          dispatch(setUser(user));
        }
      } catch (error) {
        console.error('Failed to initialize auth:', error);
        localStorage.removeItem('user');
        localStorage.removeItem('authToken');
      } finally {
        dispatch(setLoading(false));
      }
    };

    initializeAuth();
  }, [dispatch]);

  return <>{children}</>;
};

export default AuthProvider;
