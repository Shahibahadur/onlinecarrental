import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Users, Luggage, Fuel, Zap, MapPin, Star, Calendar, Shield, CheckCircle, MessageSquare } from 'lucide-react';
import { carsAPI } from '../../api/cars';
import BookingModal from '../../components/Booking/BookingModal';
import ReviewForm from '../../components/Review/ReviewForm';
import ReviewList from '../../components/Review/ReviewList';
import { formatPricePerDay } from '../../constants/locale';
import type { Car } from '../../types/car';
import type { RootState } from '../../store';

const CarDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);
  const [bookingCar, setBookingCar] = useState<Car | null>(null);
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [showReviewForm, setShowReviewForm] = useState(false);

  const { data: car, isLoading, error } = useQuery({
    queryKey: ['car', id],
    queryFn: () => carsAPI.getById(id!),
    enabled: !!id,
  });

  const carData = car?.data || car;

  const handleBookClick = () => {
    if (!isAuthenticated) {
      const confirmLogin = window.confirm(
        'You need to login or register to book this car. Do you want to go to the login page?'
      );
      if (confirmLogin) {
        navigate('/login', { state: { returnTo: `/cars/${id}` } });
      }
      return;
    }
    if (carData) {
      setBookingCar(carData);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          <p className="mt-4 text-neutral-600">Loading car details...</p>
        </div>
      </div>
    );
  }

  if (error || !carData) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-neutral-50">
        <div className="text-center">
          <p className="text-red-600 text-lg mb-4">Car not found</p>
          <Link
            to="/cars"
            className="inline-block px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            Browse All Cars
          </Link>
        </div>
      </div>
    );
  }

  const images = carData.images && carData.images.length > 0 ? carData.images : [carData.image];

  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center px-4 py-2 bg-white border border-neutral-200 rounded-lg text-neutral-700 hover:text-primary-600 hover:bg-neutral-50 mb-6 transition-colors"
        >
          <ArrowLeft className="h-5 w-5 mr-2" />
          Back to Cars
        </button>

        <div className="bg-white rounded-xl shadow-sm border border-neutral-200 overflow-hidden">
          {/* Image Gallery */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 p-6">
            <div className="space-y-4">
              <div className="relative h-96 rounded-xl overflow-hidden">
                <img
                  src={images[selectedImageIndex]}
                  alt={carData.name}
                  className="w-full h-full object-cover"
                />
              </div>
              {images.length > 1 && (
                <div className="grid grid-cols-4 gap-2">
                  {images.map((img, index) => (
                    <button
                      key={index}
                      onClick={() => setSelectedImageIndex(index)}
                      className={`relative h-20 rounded-lg overflow-hidden border-2 ${
                        selectedImageIndex === index
                          ? 'border-primary-600'
                          : 'border-transparent'
                      }`}
                    >
                      <img
                        src={img}
                        alt={`${carData.name} ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Details */}
            <div className="space-y-6 p-6">
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h1 className="text-3xl font-bold text-neutral-900 mb-2">{carData.name}</h1>
                    <p className="text-lg text-neutral-600">{carData.brand} {carData.model}</p>
                  </div>
                  <div className="text-right">
                    <div className="inline-flex items-center px-4 py-2 rounded-full bg-primary-50 text-primary-700 border border-primary-100 text-2xl font-bold">
                      {formatPricePerDay(carData.pricePerDay)}
                    </div>
                    <div className="flex items-center justify-end mt-2">
                      <Star className="h-5 w-5 text-yellow-400 fill-current" />
                      <span className="ml-1 font-medium">{carData.rating}</span>
                      <span className="ml-1 text-neutral-600">({carData.reviews} reviews)</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center text-neutral-600 mb-6">
                  <MapPin className="h-5 w-5 mr-2" />
                  <span>{carData.location}</span>
                </div>
              </div>

              {/* Specifications */}
              <div className="grid grid-cols-2 gap-4 py-6 border-t border-b border-neutral-200">
                <div className="flex items-center space-x-3">
                  <div className="bg-primary-100 p-3 rounded-lg">
                    <Users className="h-5 w-5 text-primary-600" />
                  </div>
                  <div>
                    <div className="text-sm text-neutral-600">Seats</div>
                    <div className="font-semibold text-neutral-900">{carData.seats} people</div>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="bg-primary-100 p-3 rounded-lg">
                    <Luggage className="h-5 w-5 text-primary-600" />
                  </div>
                  <div>
                    <div className="text-sm text-neutral-600">Luggage</div>
                    <div className="font-semibold text-neutral-900">{carData.luggage} bags</div>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="bg-primary-100 p-3 rounded-lg">
                    <Fuel className="h-5 w-5 text-primary-600" />
                  </div>
                  <div>
                    <div className="text-sm text-neutral-600">Fuel Type</div>
                    <div className="font-semibold text-neutral-900">{carData.fuelType}</div>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <div className="bg-primary-100 p-3 rounded-lg">
                    <Zap className="h-5 w-5 text-primary-600" />
                  </div>
                  <div>
                    <div className="text-sm text-neutral-600">Transmission</div>
                    <div className="font-semibold text-neutral-900">{carData.transmission}</div>
                  </div>
                </div>
              </div>

              {/* Features */}
              <div>
                <h3 className="text-lg font-semibold text-neutral-900 mb-4">Features & Amenities</h3>
                <div className="grid grid-cols-2 gap-3">
                  {carData.features.map((feature, index) => (
                    <div key={index} className="flex items-center">
                      <CheckCircle className="h-5 w-5 text-green-500 mr-2" />
                      <span className="text-neutral-700">{feature}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Availability & Book Button */}
              <div className="pt-6 border-t border-neutral-200">
                {carData.available ? (
                  <button
                    onClick={handleBookClick}
                    className="w-full bg-primary-600 hover:bg-primary-700 text-white py-4 rounded-lg font-semibold text-lg transition-colors shadow-sm"
                  >
                    Book This Car Now
                  </button>
                ) : (
                  <div className="text-center p-4 bg-red-50 rounded-lg border border-red-100">
                    <p className="text-red-600 font-medium">Currently Unavailable</p>
                    <p className="text-sm text-neutral-600 mt-1">Please check back later or contact us</p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Additional Info */}
          <div className="border-t border-neutral-200 p-6 bg-neutral-50">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="flex items-start space-x-3">
                <Shield className="h-6 w-6 text-primary-600 mt-1" />
                <div>
                  <h4 className="font-semibold text-neutral-900 mb-1">Insurance Included</h4>
                  <p className="text-sm text-neutral-600">Basic insurance coverage included. Optional comprehensive insurance available.</p>
                </div>
              </div>
              <div className="flex items-start space-x-3">
                <Calendar className="h-6 w-6 text-primary-600 mt-1" />
                <div>
                  <h4 className="font-semibold text-neutral-900 mb-1">Flexible Dates</h4>
                  <p className="text-sm text-neutral-600">Change or cancel your booking up to 24 hours before pickup.</p>
                </div>
              </div>
              <div className="flex items-start space-x-3">
                <MapPin className="h-6 w-6 text-primary-600 mt-1" />
                <div>
                  <h4 className="font-semibold text-neutral-900 mb-1">Multiple Locations</h4>
                  <p className="text-sm text-neutral-600">Pickup and drop-off available in multiple cities across Nepal.</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Reviews Section */}
        <div className="mt-8 bg-white rounded-xl border border-neutral-200 shadow-sm p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-neutral-900 flex items-center gap-2">
              <MessageSquare className="h-6 w-6 text-primary-600" />
              Reviews & Feedback
            </h2>
            {isAuthenticated && !showReviewForm && (
              <button
                onClick={() => setShowReviewForm(true)}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors"
              >
                Write a Review
              </button>
            )}
          </div>

          {/* Review Form */}
          {showReviewForm && isAuthenticated && (
            <div className="mb-8">
              <ReviewForm
                vehicleId={id!}
                onSuccess={() => setShowReviewForm(false)}
                onCancel={() => setShowReviewForm(false)}
              />
            </div>
          )}

          {/* Review List */}
          <ReviewList vehicleId={id!} />
        </div>
      </div>

      {/* Booking Modal */}
      {bookingCar && isAuthenticated && bookingCar.id && bookingCar.pricePerDay && (
        <BookingModal
          car={bookingCar}
          isOpen={!!bookingCar}
          onClose={() => setBookingCar(null)}
          onSuccess={() => {
            setBookingCar(null);
            navigate('/dashboard');
          }}
        />
      )}
    </div>
  );
};

export default CarDetailPage;
