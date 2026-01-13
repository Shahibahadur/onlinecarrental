import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import LoginModal from '../components/Auth/LoginModal';

const Home: React.FC = () => {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);

  const handleJoinUs = () => {
    setIsLoginModalOpen(true);
  };

  return (
    <div className="min-h-screen relative">
      {/* Hero Section with Background */}
      <section className="relative min-h-screen flex items-center">
        {/* Background Image */}
        <div 
          className="absolute inset-0 bg-cover bg-center bg-no-repeat"
          style={{
            backgroundImage: 'url(https://images.unsplash.com/photo-1449824913935-59a10b8d2000?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80)',
          }}
        >
          {/* Dark overlay for better text readability */}
          <div className="absolute inset-0 bg-black/40" />
        </div>

        {/* Content - Left Side */}
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
          <div className="max-w-2xl">
            {/* Main Headline */}
            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold mb-6">
              <span className="text-white">Rent Your</span>
              <br />
              <span className="text-primary-600">Dream Car</span>
            </h1>

            {/* Descriptive Text */}
            <div className="text-white text-lg md:text-xl space-y-2 mb-8">
              <p>Live the life of Luxury.</p>
              <p>Just rent a car of your wish from our vast collection.</p>
              <p>Enjoy every moment with your family</p>
              <p>Join us to make this family vast.</p>
            </div>

            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={handleJoinUs}
                className="px-8 py-3 bg-primary-600 text-white rounded font-medium text-lg hover:bg-primary-700 transition-colors uppercase"
              >
                Join Us
              </button>
              <Link
                to="/cars-grid"
                className="px-8 py-3 bg-white text-neutral-900 rounded font-medium text-lg hover:bg-neutral-100 transition-colors uppercase text-center"
              >
                Browse Cars
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-white border-t border-neutral-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="rounded-lg border border-neutral-200 p-6">
              <div className="text-sm font-semibold text-neutral-900">Wide Selection</div>
              <div className="text-sm text-neutral-600 mt-2">Choose from a curated fleet across Nepal.</div>
            </div>
            <div className="rounded-lg border border-neutral-200 p-6">
              <div className="text-sm font-semibold text-neutral-900">Fast Booking</div>
              <div className="text-sm text-neutral-600 mt-2">Book in minutes and pay securely.</div>
            </div>
            <div className="rounded-lg border border-neutral-200 p-6">
              <div className="text-sm font-semibold text-neutral-900">Trusted Support</div>
              <div className="text-sm text-neutral-600 mt-2">We’re here to help throughout your trip.</div>
            </div>
          </div>
        </div>
      </section>

      {/* Login Modal */}
      <LoginModal
        isOpen={isLoginModalOpen}
        onClose={() => setIsLoginModalOpen(false)}
      />
    </div>
  );
};

export default Home;
