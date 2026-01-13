import React from 'react';
import { Shield, Clock, Star, Users, Award, Heart } from 'lucide-react';

const About: React.FC = () => {
  return (
    <div className="min-h-screen bg-neutral-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-primary-600 to-primary-700 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">About DriveRental Nepal</h1>
          <p className="text-xl text-primary-100 max-w-3xl">
            Your trusted partner for car rentals in Nepal since 2020
          </p>
        </div>
      </section>

      {/* Story Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-3xl font-bold text-neutral-900 mb-6">Our Story</h2>
              <p className="text-neutral-600 mb-4 leading-relaxed">
                DriveRental Nepal was founded with a simple mission: to make car rental in Nepal 
                accessible, affordable, and reliable for both locals and tourists. We understand 
                the unique challenges of traveling in Nepal's diverse terrain, from the bustling 
                streets of Kathmandu to the winding mountain roads.
              </p>
              <p className="text-neutral-600 mb-4 leading-relaxed">
                With years of experience in the tourism and transportation industry, our team 
                has built a reputation for exceptional service, well-maintained vehicles, and 
                competitive pricing. We're committed to helping you explore the beautiful 
                landscapes of Nepal safely and comfortably.
              </p>
              <p className="text-neutral-600 leading-relaxed">
                Whether you're planning a family trip, a business journey, or an adventure 
                across the Himalayas, we're here to make your travel experience memorable 
                and hassle-free.
              </p>
            </div>
            <div className="bg-neutral-100 rounded-lg p-8">
              <div className="grid grid-cols-2 gap-6">
                <div className="text-center">
                  <div className="text-4xl font-bold text-primary-600 mb-2">1000+</div>
                  <div className="text-neutral-600">Happy Customers</div>
                </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-primary-600 mb-2">50+</div>
                  <div className="text-neutral-600">Vehicles</div>
                </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-primary-600 mb-2">15+</div>
                  <div className="text-neutral-600">Cities Covered</div>
                </div>
                <div className="text-center">
                  <div className="text-4xl font-bold text-primary-600 mb-2">24/7</div>
                  <div className="text-neutral-600">Support</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Values Section */}
      <section className="py-16 bg-neutral-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-neutral-900 mb-12 text-center">Our Values</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {[
              {
                icon: Shield,
                title: 'Safety First',
                description: 'All our vehicles undergo regular maintenance and safety inspections to ensure your journey is safe and secure.',
              },
              {
                icon: Heart,
                title: 'Customer Care',
                description: 'We prioritize our customers\' satisfaction and are always ready to go the extra mile to help.',
              },
              {
                icon: Award,
                title: 'Quality Service',
                description: 'We maintain high standards in every aspect of our service, from vehicle quality to customer support.',
              },
              {
                icon: Users,
                title: 'Local Expertise',
                description: 'Our team consists of locals who know Nepal inside out and can provide valuable insights and recommendations.',
              },
              {
                icon: Star,
                title: 'Affordable Prices',
                description: 'We offer competitive pricing with no hidden charges, making car rental accessible to everyone.',
              },
              {
                icon: Clock,
                title: 'Reliability',
                description: 'We value punctuality and reliability. When you book with us, you can count on us to be there.',
              },
            ].map((value, index) => (
              <div key={index} className="bg-white p-6 rounded-lg shadow-sm">
                <div className="bg-primary-100 w-12 h-12 rounded-full flex items-center justify-center mb-4">
                  <value.icon className="h-6 w-6 text-primary-600" />
                </div>
                <h3 className="text-xl font-semibold text-neutral-900 mb-2">{value.title}</h3>
                <p className="text-neutral-600">{value.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Team Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-neutral-900 mb-12 text-center">Why Choose Us?</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="bg-neutral-50 p-6 rounded-lg">
              <h3 className="text-xl font-semibold text-neutral-900 mb-4">Wide Selection</h3>
              <p className="text-neutral-600">
                From compact cars for city travel to SUVs for mountain adventures, we have 
                vehicles to suit every need and budget. All our cars are regularly serviced 
                and in excellent condition.
              </p>
            </div>
            <div className="bg-neutral-50 p-6 rounded-lg">
              <h3 className="text-xl font-semibold text-neutral-900 mb-4">Flexible Options</h3>
              <p className="text-neutral-600">
                Choose between self-drive or with a professional driver. Our drivers are 
                experienced, licensed, and familiar with Nepal's roads and destinations.
              </p>
            </div>
            <div className="bg-neutral-50 p-6 rounded-lg">
              <h3 className="text-xl font-semibold text-neutral-900 mb-4">Easy Booking</h3>
              <p className="text-neutral-600">
                Our online booking system makes it easy to reserve a car in just a few clicks. 
                You can also call or WhatsApp us for assistance with your booking.
              </p>
            </div>
            <div className="bg-neutral-50 p-6 rounded-lg">
              <h3 className="text-xl font-semibold text-neutral-900 mb-4">Multiple Payment Methods</h3>
              <p className="text-neutral-600">
                Pay via eSewa, Khalti, bank transfer, or cash. We accept all major payment 
                methods for your convenience.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default About;
