import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { authAPI } from '../../api/auth';
import { setUser } from '../../store/slices/authSlice';

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const LoginModal: React.FC<LoginModalProps> = ({ isOpen, onClose }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const response = await authAPI.login({
        email,
        password,
      });

      localStorage.setItem('authToken', response.data.token);

      const user = {
        id: response.data.email,
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        phone: '',
      };

      dispatch(setUser(user));
      onClose();
      navigate('/dashboard');
    } catch (err: any) {
      let errorMessage = 'Login failed. Please check your credentials.';
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.message) {
        errorMessage = err.message;
      }
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <>
      {/* Dark overlay */}
      <div
        className="fixed inset-0 bg-black/70 z-40"
        onClick={onClose}
      />
      
      {/* Modal - positioned on right side */}
      <div className="fixed right-8 top-1/2 -translate-y-1/2 z-50 bg-neutral-800 rounded-lg w-full max-w-md overflow-hidden">
        {/* Orange header */}
        <div className="bg-primary-600 px-6 py-4">
          <h2 className="text-xl font-bold text-white text-center">Login Here</h2>
        </div>

        {/* Form body */}
        <div className="px-6 py-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="bg-red-500/20 border border-red-500 text-white px-4 py-3 rounded text-sm">
                {error}
              </div>
            )}

            {/* Email input */}
            <div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter Email Here"
                className="w-full bg-transparent border-0 border-b-2 border-neutral-700 focus:border-primary-600 text-white placeholder-neutral-500 px-2 py-3 focus:outline-none transition-colors"
                required
              />
            </div>

            {/* Password input */}
            <div>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter Password Here"
                className="w-full bg-transparent border-0 border-b-2 border-neutral-700 focus:border-primary-600 text-white placeholder-neutral-500 px-2 py-3 focus:outline-none transition-colors"
                required
              />
            </div>

            {/* Login button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-primary-600 text-white py-3 rounded font-medium hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          {/* Sign up link */}
          <p className="mt-4 text-center text-white text-sm">
            Don't have an account?{' '}
            <Link
              to="/register"
              onClick={onClose}
              className="text-primary-600 hover:text-primary-500 font-medium"
            >
              Sign up here
            </Link>
          </p>
        </div>
      </div>
    </>
  );
};

export default LoginModal;
