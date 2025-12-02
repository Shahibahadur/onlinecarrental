import React from 'react';
import LoginForm from '../../components/Auth/LoginForm';

const Login: React.FC = () => {
  const handleLogin = (data: any) => {
    console.log('Login:', data);
    // Implement login logic
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-neutral-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-bold text-neutral-900">
            Sign in to your account
          </h2>
        </div>
        <LoginForm onSubmit={handleLogin} />
        <p className="text-center text-sm text-neutral-600">
          Don't have an account?{' '}
          <a href="/register" className="text-primary-600 hover:text-primary-700 font-medium">
            Register now
          </a>
        </p>
      </div>
    </div>
  );
};

export default Login;
