import React from 'react';
import RegisterForm from '../../components/Auth/RegisterForm';

const Register: React.FC = () => {
  const handleRegister = (data: any) => {
    console.log('Register:', data);
    // Implement registration logic
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-neutral-900">
            Create your account
          </h2>
        </div>
        <RegisterForm onSubmit={handleRegister} />
        <p className="text-center text-sm text-neutral-600">
          Already have an account?{' '}
          <a href="/login" className="text-primary-600 hover:text-primary-700 font-medium">
            Sign in
          </a>
        </p>
      </div>
    </div>
  );
};

export default Register;
