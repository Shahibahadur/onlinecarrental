import React from 'react';

const NotFound: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-neutral-900 mb-4">404</h1>
        <p className="text-xl text-neutral-600 mb-8">Page not found</p>
        <a
          href="/"
          className="inline-block bg-primary-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-primary-700 transition-colors"
        >
          Go Home
        </a>
      </div>
    </div>
  );
};

export default NotFound;
