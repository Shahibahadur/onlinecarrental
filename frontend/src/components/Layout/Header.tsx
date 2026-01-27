import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { Menu, X } from 'lucide-react';
import type  { RootState } from '../../store';
import { clearUser } from '../../store/slices/authSlice';

const Header: React.FC = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(clearUser());
    setIsMenuOpen(false);
  };

  const navigation = [
    { name: 'HOME', href: '/' },
    { name: 'CARS', href: '/cars-grid' },
    { name: 'ABOUT', href: '/about' },
    { name: 'SERVICES', href: '/services' },
    { name: 'CONTACT', href: '/contact' },
  ];

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur border-b border-neutral-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo - CaRs */}
          <Link to="/" className="flex items-center">
            <span className="text-2xl font-bold">
              <span className="text-primary-600">Ca</span>
              <span className="text-neutral-900">Rs</span>
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex space-x-8">
            {navigation.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className={`text-sm font-medium uppercase transition-colors ${
                  location.pathname === item.href
                    ? 'text-primary-600'
                    : 'text-neutral-900 hover:text-primary-600'
                }`}
              >
                {item.name}
              </Link>
            ))}
          </nav>

          <div className="hidden md:flex items-center">
            {isAuthenticated ? (
              <div className="flex items-center space-x-3">
                {user?.role?.toUpperCase() === 'ADMIN' ? (
                  <Link
                    to="/admin"
                    className="px-4 py-2 text-sm font-medium text-neutral-900 hover:text-primary-600 transition-colors"
                  >
                    Admin Panel
                  </Link>
                ) : (
                  <>
                    <Link
                      to="/dashboard"
                      className="px-4 py-2 text-sm font-medium text-neutral-900 hover:text-primary-600 transition-colors"
                    >
                      Dashboard
                    </Link>
                    <Link
                      to="/my-reservations"
                      className="px-4 py-2 text-sm font-medium text-neutral-900 hover:text-primary-600 transition-colors"
                    >
                      My Reservations
                    </Link>
                  </>
                )}
                <button
                  onClick={handleLogout}
                  className="px-4 py-2 text-sm font-medium text-neutral-900 hover:text-primary-600 transition-colors"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => navigate('/login')}
                  className="px-4 py-2 text-sm font-medium text-neutral-900 hover:text-primary-600 transition-colors uppercase"
                >
                  Login
                </button>
                <button
                  onClick={() => navigate('/register')}
                  className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded hover:bg-primary-700 transition-colors uppercase"
                >
                  Register
                </button>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <button
            className="md:hidden p-2"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>

        {/* Mobile Navigation */}
        {isMenuOpen && (
          <div className="md:hidden py-4 border-t border-neutral-200">
            <div className="flex flex-col space-y-2">
              {navigation.map((item) => (
                <Link
                  key={item.name}
                  to={item.href}
                  className="px-3 py-2 text-base font-medium uppercase text-neutral-900 hover:text-primary-600"
                  onClick={() => setIsMenuOpen(false)}
                >
                  {item.name}
                </Link>
              ))}
              {isAuthenticated ? (
                <>
                  {user?.role?.toUpperCase() === 'ADMIN' ? (
                    <Link
                      to="/admin"
                      className="px-3 py-2 text-base font-medium text-neutral-900 hover:text-primary-600"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      Admin Panel
                    </Link>
                  ) : (
                    <>
                      <Link
                        to="/dashboard"
                        className="px-3 py-2 text-base font-medium text-neutral-900 hover:text-primary-600"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Dashboard
                      </Link>
                      <Link
                        to="/my-reservations"
                        className="px-3 py-2 text-base font-medium text-neutral-900 hover:text-primary-600"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        My Reservations
                      </Link>
                    </>
                  )}
                  <button
                    onClick={handleLogout}
                    className="px-3 py-2 text-left text-base font-medium text-neutral-900 hover:text-primary-600"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={() => {
                      navigate('/login');
                      setIsMenuOpen(false);
                    }}
                    className="px-3 py-2 text-left text-base font-medium text-neutral-900 hover:text-primary-600"
                  >
                    Login
                  </button>
                  <button
                    onClick={() => {
                      navigate('/register');
                      setIsMenuOpen(false);
                    }}
                    className="px-3 py-2 text-base font-medium text-white bg-primary-600 rounded hover:bg-primary-700 uppercase"
                  >
                    Register
                  </button>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;