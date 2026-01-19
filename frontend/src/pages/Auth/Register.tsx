import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import RegisterForm from '../../components/Auth/RegisterForm';
import { authAPI } from '../../api/auth';
import { setUser } from '../../store/slices/authSlice';

const Register: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  // Get return URL from navigation state or URL params
  const getReturnUrl = () => {
    const state = location.state as { returnTo?: string } | null;
    const params = new URLSearchParams(location.search);
    return state?.returnTo || params.get('returnTo') || '/dashboard';
  };

  const handleRegister = async (data: any) => {
    try {
      setError(null);
      setIsLoading(true);
      
      console.log('Attempting registration with:', { email: data.email });
      
      // Call the actual API
      const response = await authAPI.register({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
        phone: data.phone,
      });
      
      console.log('Registration response:', response.data);
      
      // Store the token
      localStorage.setItem('authToken', response.data.token);
      
      // Store user data in Redux
      const user = {
        id: response.data.email, // Using email as ID temporarily
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        phone: data.phone,
      };
      
      dispatch(setUser(user));
      
      // Redirect to return URL or dashboard
      const returnUrl = getReturnUrl();
      navigate(returnUrl, { replace: true });
    } catch (err: any) {
      console.error('Registration error:', err);
      console.error('Error response:', err.response);
      console.error('Error data:', err.response?.data);
      
      // Extract error message from different possible locations
      let errorMessage = 'Registration failed. Please try again.';
      
      if (err.response?.data) {
        if (err.response.data.message) {
          errorMessage = err.response.data.message;
        } else if (err.response.data.error) {
          errorMessage = err.response.data.error;
        } else if (typeof err.response.data === 'string') {
          errorMessage = err.response.data;
        }
      } else if (err.message) {
        if (err.message.includes('Network Error') || err.message.includes('ERR_CONNECTION_REFUSED') || err.message.includes('Failed to fetch')) {
          errorMessage = `Cannot connect to server. Please make sure the backend is running on ${import.meta.env.VITE_API_BASE || 'http://localhost:8080'}`;
        } else if (err.message.includes('CORS')) {
          errorMessage = 'CORS error. Please check backend CORS configuration.';
        } else {
          errorMessage = err.message;
        }
      } else if (err.code === 'ERR_NETWORK') {
        errorMessage = 'Network error. Please check if the backend server is running.';
      }
      
      console.error('Final error message:', errorMessage);
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-neutral-900">
            Create your account
          </h2>
        </div>
        
        {error && (
          <div className="rounded-md bg-red-50 p-4">
            <div className="text-sm text-red-800">{error}</div>
          </div>
        )}
        
        <RegisterForm onSubmit={handleRegister} isLoading={isLoading} />
        <p className="text-center text-sm text-neutral-600">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
