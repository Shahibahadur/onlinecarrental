import React from 'react';
import { Link } from 'react-router-dom';
import { Search, Shield, Clock, Star, MapPin } from 'lucide-react';
import SearchFilters from '../components/Search/SearchFilters';
import { CONTENT } from '../constants/locale';

const Home: React.FC = () => {
  const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
    Shield,
    Clock,
    Star,
    MapPin,
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center">
            <h1 className="text-5xl md:text-7xl font-extrabold mb-6 text-red-500 bg-black py-4 rounded-xl shadow-2xl">
              {CONTENT.hero.title}
              <span className="block text-yellow-300">{CONTENT.hero.titleHighlight}</span>
            </h1>
            <p className="text-xl md:text-2xl text-primary-100 mb-8 max-w-3xl mx-auto">
              {CONTENT.hero.subtitle}
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
              {CONTENT.features.title}
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              {CONTENT.features.subtitle}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {CONTENT.features.items.map((feature, index) => {
              const IconComponent = iconMap[feature.icon] || Shield;
              return (
                <div key={index} className="text-center">
                  <div className="bg-primary-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                    <IconComponent className="h-8 w-8 text-primary-600" />
                  </div>
                  <h3 className="text-xl font-semibold text-neutral-900 mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-neutral-600">
                    {feature.description}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Popular Destinations Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-neutral-900 mb-4">
              Popular Destinations in Nepal
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              Explore the beautiful destinations of Nepal with our reliable car rental service
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { name: 'Kathmandu Valley', desc: 'Explore ancient temples and rich culture' },
              { name: 'Pokhara', desc: 'Gateway to the Annapurna region' },
              { name: 'Chitwan', desc: 'Wildlife safari and jungle adventures' },
              { name: 'Lumbini', desc: 'Birthplace of Lord Buddha' },
              { name: 'Nagarkot', desc: 'Mountain views and sunrise' },
              { name: 'Bhaktapur', desc: 'Medieval city and heritage sites' },
            ].map((dest, index) => (
              <div key={index} className="bg-white rounded-lg p-6 shadow-sm border border-neutral-200 hover:shadow-md transition-shadow">
                <h3 className="text-lg font-semibold text-neutral-900 mb-2">{dest.name}</h3>
                <p className="text-neutral-600">{dest.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-neutral-900 mb-4">
            {CONTENT.cta.title}
          </h2>
          <p className="text-lg text-neutral-600 mb-8 max-w-2xl mx-auto">
            {CONTENT.cta.subtitle}
          </p>
          <Link
            to="/cars"
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 transition-colors"
          >
            <Search className="h-5 w-5 mr-2" />
            {CONTENT.cta.button}
          </Link>
        </div>
      </section>
    </div>
  );
};

export default Home;