import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Shield, Clock, Star, Users, MapPin, Calendar, Phone, TrendingUp, Award, Zap } from 'lucide-react';
import EnhancedSearchForm from '../components/Search/EnhancedSearchForm';

const Home: React.FC = () => {
  const [stats, setStats] = useState({
    vehicles: 0,
    customers: 0,
    drivers: 0,
    years: 0,
  });

  useEffect(() => {
    // Animate counters
    const targets = { vehicles: 5000, customers: 200000, drivers: 5000, years: 30 };
    const duration = 2000;
    const steps = 60;
    const stepTime = duration / steps;

    Object.keys(targets).forEach((key) => {
      const target = targets[key as keyof typeof targets];
      let current = 0;
      const increment = target / steps;
      const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
          current = target;
          clearInterval(timer);
        }
        setStats((prev) => ({ ...prev, [key]: Math.floor(current) }));
      }, stepTime);
    });
  }, []);

  const tourPackages = [
    {
      id: 1,
      title: 'Weekend Getaway',
      location: 'Mountain View',
      duration: '2 Days',
      price: 299,
      image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400',
    },
    {
      id: 2,
      title: 'Coastal Adventure',
      location: 'Beach Paradise',
      duration: '3 Days',
      price: 449,
      image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400',
    },
    {
      id: 3,
      title: 'City Explorer',
      location: 'Urban Experience',
      duration: '1 Day',
      price: 199,
      image: 'https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=400',
    },
  ];

  const blogs = [
    {
      id: 1,
      title: 'Top 10 Road Trip Destinations for 2024',
      excerpt: 'Discover the most scenic routes and must-visit destinations for your next adventure.',
      image: 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400',
      date: 'March 15, 2024',
    },
    {
      id: 2,
      title: 'Electric Vehicles: The Future of Car Rentals',
      excerpt: 'Learn about the benefits of renting electric vehicles and our growing EV fleet.',
      image: 'https://images.unsplash.com/photo-1593941707882-a5bac6861d75?w=400',
      date: 'March 10, 2024',
    },
    {
      id: 3,
      title: 'Safety Tips for Long-Distance Driving',
      excerpt: 'Essential safety guidelines to ensure a smooth and secure journey.',
      image: 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=400',
      date: 'March 5, 2024',
    },
  ];

  const specialPackages = [
    {
      id: 1,
      name: 'Luxury Collection',
      description: 'Premium vehicles for special occasions',
      icon: Award,
      color: 'from-purple-500 to-pink-500',
    },
    {
      id: 2,
      name: 'Family Friendly',
      description: 'Spacious SUVs perfect for family trips',
      icon: Users,
      color: 'from-blue-500 to-cyan-500',
    },
    {
      id: 3,
      name: 'Eco-Friendly',
      description: 'Electric and hybrid vehicles',
      icon: Zap,
      color: 'from-green-500 to-emerald-500',
    },
    {
      id: 4,
      name: 'Business Class',
      description: 'Professional vehicles for business travel',
      icon: TrendingUp,
      color: 'from-orange-500 to-red-500',
    },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section with Enhanced Search */}
      <section className="bg-gradient-to-br from-primary-600 via-primary-700 to-primary-800 text-white relative overflow-hidden">
        <div className="absolute inset-0 bg-black opacity-10"></div>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 md:py-24 relative z-10">
          <div className="text-center mb-12">
            <h1 className="text-4xl md:text-6xl lg:text-7xl font-extrabold mb-6">
              Find Your Perfect
              <span className="block text-primary-200 mt-2">Rental Car</span>
            </h1>
            <p className="text-xl md:text-2xl text-primary-100 max-w-3xl mx-auto mb-8">
              Discover the best rental deals from trusted providers. Quality vehicles at competitive prices.
            </p>
          </div>
          
          {/* Enhanced Search Form */}
          <div className="max-w-6xl mx-auto">
            <EnhancedSearchForm />
          </div>
        </div>
      </section>

      {/* Statistics Section */}
      <section className="py-16 bg-white border-b border-neutral-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            <div className="text-center">
              <div className="text-4xl md:text-5xl font-bold text-primary-600 mb-2">
                {stats.vehicles.toLocaleString()}+
              </div>
              <div className="text-neutral-600 font-medium">No of Vehicles</div>
            </div>
            <div className="text-center">
              <div className="text-4xl md:text-5xl font-bold text-primary-600 mb-2">
                {stats.customers.toLocaleString()}+
              </div>
              <div className="text-neutral-600 font-medium">Customers Served Annually</div>
            </div>
            <div className="text-center">
              <div className="text-4xl md:text-5xl font-bold text-primary-600 mb-2">
                {stats.drivers.toLocaleString()}+
              </div>
              <div className="text-neutral-600 font-medium">No of Drivers</div>
            </div>
            <div className="text-center">
              <div className="text-4xl md:text-5xl font-bold text-primary-600 mb-2">
                {stats.years}+
              </div>
              <div className="text-neutral-600 font-medium">Years of Experience</div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-neutral-900 mb-4">
              Why Choose DriveRental?
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              We provide exceptional service and quality vehicles to make your journey comfortable and memorable.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white p-8 rounded-xl shadow-sm hover:shadow-md transition-shadow text-center">
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

            <div className="bg-white p-8 rounded-xl shadow-sm hover:shadow-md transition-shadow text-center">
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

            <div className="bg-white p-8 rounded-xl shadow-sm hover:shadow-md transition-shadow text-center">
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

      {/* Tour Packages Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-neutral-900 mb-4">
              Tour Packages
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              Explore our curated tour packages designed for unforgettable journeys.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {tourPackages.map((pkg) => (
              <div
                key={pkg.id}
                className="bg-white rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-shadow"
              >
                <div className="relative h-48">
                  <img
                    src={pkg.image}
                    alt={pkg.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute top-4 right-4 bg-white px-3 py-1 rounded-full text-sm font-semibold text-primary-600">
                    ${pkg.price}
                  </div>
                </div>
                <div className="p-6">
                  <h3 className="text-xl font-bold text-neutral-900 mb-2">{pkg.title}</h3>
                  <div className="flex items-center text-neutral-600 mb-3">
                    <MapPin className="h-4 w-4 mr-2" />
                    <span>{pkg.location}</span>
                    <span className="mx-2">•</span>
                    <Calendar className="h-4 w-4 mr-2" />
                    <span>{pkg.duration}</span>
                  </div>
                  <Link
                    to="/cars"
                    className="inline-block w-full text-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
                  >
                    Book Now
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Latest Blogs Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-neutral-900 mb-4">
              Latest Blogs
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              Stay updated with the latest travel tips, news, and insights.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {blogs.map((blog) => (
              <article
                key={blog.id}
                className="bg-white rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-shadow"
              >
                <div className="relative h-48">
                  <img
                    src={blog.image}
                    alt={blog.title}
                    className="w-full h-full object-cover"
                  />
                </div>
                <div className="p-6">
                  <div className="text-sm text-neutral-500 mb-2">{blog.date}</div>
                  <h3 className="text-xl font-bold text-neutral-900 mb-3">{blog.title}</h3>
                  <p className="text-neutral-600 mb-4">{blog.excerpt}</p>
                  <Link
                    to="#"
                    className="text-primary-600 font-medium hover:text-primary-700"
                  >
                    Read More →
                  </Link>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* Special Packages Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-neutral-900 mb-4">
              Our Special Packages
            </h2>
            <p className="text-lg text-neutral-600 max-w-2xl mx-auto">
              Where your journey begins with an exquisite fleet of vehicles for an unforgettable experience.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {specialPackages.map((pkg) => {
              const Icon = pkg.icon;
              return (
                <div
                  key={pkg.id}
                  className={`bg-gradient-to-br ${pkg.color} rounded-xl p-6 text-white hover:scale-105 transition-transform cursor-pointer shadow-lg`}
                >
                  <Icon className="h-12 w-12 mb-4" />
                  <h3 className="text-xl font-bold mb-2">{pkg.name}</h3>
                  <p className="text-white/90">{pkg.description}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Contact/Call Section */}
      <section className="py-16 bg-gradient-to-r from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Have Any Questions?
          </h2>
          <p className="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
            Call us for further information. Customer care is here to help you anytime.
          </p>
          <a
            href="tel:+1234567890"
            className="inline-flex items-center px-8 py-4 bg-white text-primary-600 rounded-lg font-semibold text-lg hover:bg-primary-50 transition-colors shadow-lg hover:shadow-xl"
          >
            <Phone className="h-6 w-6 mr-3" />
            Call Us Now
          </a>
          <div className="mt-6">
            <Link
              to="/contact"
              className="text-primary-100 hover:text-white underline font-medium"
            >
              Contact Us
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;
