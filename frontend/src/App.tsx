import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { store } from './store';
import Layout from './components/Layout/Layout';
import ProtectedRoute from './components/Layout/ProtectedRoute';

// Pages
import Home from './pages/Home';
import NotFound from './pages/NotFound';
import About from './pages/About';
import Services from './pages/Services';
import Contact from './pages/Contact';
import EsewaSuccess from './pages/Payment/EsewaSuccess';
import EsewaFailure from './pages/Payment/EsewaFailure';
import EsewaCheckout from './pages/Payment/EsewaCheckout';

// Auth Pages
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';

// Car Pages
import CarListingPage from './pages/CarListingPage';
import Cars from './pages/Cars';
import CarDetailPage from './pages/Car/CarDetailPage';
import SearchResults from './pages/Car/SearchResults';

// User Pages
import UserDashboard from './pages/User/Dashboard';
import MyReservations from './pages/User/MyReservations';

// Admin Pages
import AdminDashboard from './pages/Admin/AdminDashboard';
import BookingManagement from './pages/Admin/BookingManagement';
import UserManagement from './pages/Admin/UserManagement';
import FeedbackManagement from './pages/Admin/FeedbackManagement';


const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <Router>
          <Layout>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Home />} />
              <Route path="/about" element={<About />} />
              <Route path="/services" element={<Services />} />
              <Route path="/contact" element={<Contact />} />
              <Route path="/cars" element={<CarListingPage />} />
              <Route path="/cars-grid" element={<Cars />} />
              <Route path="/cars/:id" element={<CarDetailPage />} />
              <Route path="/search" element={<SearchResults />} />

              {/* Payment Routes */}
              <Route path="/esewa/checkout" element={<EsewaCheckout />} />
              <Route path="/esewa/success" element={<EsewaSuccess />} />
              <Route path="/esewa/failure" element={<EsewaFailure />} />

              {/* Auth Routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* User Routes */}
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <UserDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-reservations"
                element={
                  <ProtectedRoute>
                    <MyReservations />
                  </ProtectedRoute>
                }
              />

              {/* Admin Routes */}
              <Route
                path="/admin"
                element={
                  <ProtectedRoute adminOnly>
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/bookings"
                element={
                  <ProtectedRoute adminOnly>
                    <BookingManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute adminOnly>
                    <UserManagement />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/feedback"
                element={
                  <ProtectedRoute adminOnly>
                    <FeedbackManagement />
                  </ProtectedRoute>
                }
              />

              {/* 404 Not Found */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Layout>
        </Router>
      </QueryClientProvider>
    </Provider>
  );
}

export default App;