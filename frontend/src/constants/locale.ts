// Localization constants for Nepali market
export const LOCALE = {
  currency: 'NPR',
  currencySymbol: 'रू',
  country: 'Nepal',
  language: 'en', // Can be extended to support 'ne' (Nepali) later
  
  // Popular destinations in Nepal
  popularDestinations: [
    { name: 'Kathmandu Valley', description: 'Explore ancient temples and rich culture' },
    { name: 'Pokhara', description: 'Gateway to the Annapurna region' },
    { name: 'Chitwan', description: 'Wildlife safari and jungle adventures' },
    { name: 'Lumbini', description: 'Birthplace of Lord Buddha' },
    { name: 'Nagarkot', description: 'Mountain views and sunrise' },
    { name: 'Bhaktapur', description: 'Medieval city and heritage sites' },
  ],

  // Local features and services
  localFeatures: [
    'Driver available (optional)',
    'Mountain road ready',
    '24/7 roadside assistance',
    'Local SIM card support',
    'Tourist permit assistance',
    'Multi-language support',
  ],

  // Popular car brands in Nepal
  popularBrands: [
    'Toyota', 'Hyundai', 'Suzuki', 'Mahindra', 'Tata', 'Honda', 'Nissan', 'Ford'
  ],

  // Contact information
  contact: {
    phone: '+977-1-XXXXXXX',
    email: 'info@driverental.com.np',
    address: 'Thamel, Kathmandu, Nepal',
    workingHours: '6:00 AM - 8:00 PM (NPT)',
  },

  // Social media (Nepali market)
  socialMedia: {
    facebook: 'https://facebook.com/driverentalnepal',
    instagram: 'https://instagram.com/driverentalnepal',
    tiktok: 'https://tiktok.com/@driverentalnepal',
  },
};

// Text content for Nepali market
export const CONTENT = {
  hero: {
    title: 'Find Your Perfect',
    titleHighlight: 'Rental Car in Nepal',
    subtitle: 'Explore the beautiful landscapes of Nepal with our reliable and affordable car rental service. Quality vehicles at competitive prices.',
    cta: 'Start Your Journey',
  },
  features: {
    title: 'Why Choose DriveRental Nepal?',
    subtitle: 'We provide exceptional service and quality vehicles to make your journey through Nepal comfortable and memorable.',
    items: [
      {
        title: 'Trusted & Safe',
        description: 'All our vehicles are regularly maintained and thoroughly inspected for your safety on Nepal\'s diverse terrains.',
        icon: 'Shield',
      },
      {
        title: '24/7 Support',
        description: 'Our customer support team is available round the clock to assist you, even in remote areas of Nepal.',
        icon: 'Clock',
      },
      {
        title: 'Best Prices',
        description: 'Competitive pricing in Nepalese Rupees with no hidden charges. Best rates for locals and tourists.',
        icon: 'Star',
      },
      {
        title: 'Local Expertise',
        description: 'Our team knows Nepal inside out. Get recommendations for routes, destinations, and local insights.',
        icon: 'MapPin',
      },
    ],
  },
  cta: {
    title: 'Ready to Explore Nepal?',
    subtitle: 'Browse our extensive collection of vehicles perfect for Nepal\'s roads and find the perfect car for your adventure.',
    button: 'Browse All Cars',
  },
  footer: {
    description: 'Your trusted partner for car rentals in Nepal. We offer the best vehicles at competitive prices with exceptional customer service. Explore Nepal with confidence.',
    quickLinks: [
      { name: 'Browse Cars', href: '/cars' },
      { name: 'About Us', href: '/about' },
      { name: 'Contact', href: '/contact' },
      { name: 'FAQ', href: '/faq' },
      { name: 'Terms & Conditions', href: '/terms' },
      { name: 'Privacy Policy', href: '/privacy' },
    ],
  },
};

// Format currency for Nepali market
export const formatCurrency = (amount: number): string => {
  return `रू ${amount.toLocaleString('en-NP')}`;
};

// Format price per day
export const formatPricePerDay = (amount: number): string => {
  return `${formatCurrency(amount)}/day`;
};



