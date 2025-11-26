import React from 'react';
import { Link } from 'react-router-dom';
import { Search, Shield, Clock, Star } from 'lucide-react';
import SearchFilters from '../components/Search/SearchFilters';

const Home: React.FC = () => {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              Find Your Perfect
              <span className="block text-primary-200">Rental Car</span>
            </h1>
            <p className="text-xl md:text-2xl text-primary-100 mb-8 max-w-3xl mx-auto">
              Discover the best rental deals from trusted providers. Quality vehicles at competitive prices.
            </p>
            
            {/* Quick Search */}
            <div className="bg-white rounded-lg p-2 max-w-4xl mx-auto shadow-lg">
              <SearchFilters />
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-neutral-900 mb-4">
              Why Choose DriveRental?
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              We provide exceptional service and quality vehicles to make your journey comfortable and memorable.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Shield className="h-8 w-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                Trusted & Safe
              </h3>
              <p className="text-neutral-600">
                All our vehicles are regularly maintained and thoroughly inspected for your safety.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Clock className="h-8 w-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                24/7 Support
              </h3>
              <p className="text-neutral-600">
                Our customer support team is available round the clock to assist you.
              </p>
            </div>

            <div className="text-center">
              <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                <Star className="h-8 w-8 text-primary-600" />
              </div>
              <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                Best Prices
              </h3>
              <p className="text-neutral-600">
                Competitive pricing with no hidden charges. Price match guarantee.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-neutral-900 mb-4">
            Ready to Start Your Journey?
          </h2>
          <p className="text-lg text-neutral-600 mb-8 max-w-2xl mx-auto">
            Browse our extensive collection of vehicles and find the perfect car for your needs.
          </p>
          <Link
            to="/cars"
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 transition-colors"
          >
            <Search className="h-5 w-5 mr-2" />
            Browse All Cars
          </Link>
        </div>
      </section>
    </div>
  );
};

export default Home;