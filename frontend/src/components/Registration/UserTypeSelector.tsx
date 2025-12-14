import React from 'react';
import { User, UserCheck, Building2 } from 'lucide-react';

export type UserType = 'guest' | 'user' | 'car_owner';

interface UserTypeSelectorProps {
  selectedType: UserType | null;
  onSelect: (type: UserType) => void;
}

const UserTypeSelector: React.FC<UserTypeSelectorProps> = ({ selectedType, onSelect }) => {
  const userTypes = [
    {
      id: 'guest' as UserType,
      title: 'Guest User',
      description: 'Register as a normal user to book cars',
      icon: User,
      color: 'bg-blue-50 border-blue-200 text-blue-700',
      selectedColor: 'bg-blue-100 border-blue-500',
    },
    {
      id: 'user' as UserType,
      title: 'Registered User',
      description: 'Standard customer account with full features',
      icon: UserCheck,
      color: 'bg-green-50 border-green-200 text-green-700',
      selectedColor: 'bg-green-100 border-green-500',
    },
    {
      id: 'car_owner' as UserType,
      title: 'Car Owner',
      description: 'List your cars for rent and earn money',
      icon: Building2,
      color: 'bg-purple-50 border-purple-200 text-purple-700',
      selectedColor: 'bg-purple-100 border-purple-500',
    },
  ];

  return (
    <div className="space-y-4">
      <div className="text-center mb-6">
        <h3 className="text-lg font-semibold text-neutral-900 mb-2">
          Select Your Account Type
        </h3>
        <p className="text-sm text-neutral-600">
          Choose the type of account that best fits your needs
        </p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {userTypes.map((type) => {
          const Icon = type.icon;
          const isSelected = selectedType === type.id;
          
          return (
            <button
              key={type.id}
              type="button"
              onClick={() => onSelect(type.id)}
              className={`
                relative p-6 rounded-lg border-2 transition-all duration-200
                ${isSelected 
                  ? `${type.selectedColor} border-2 shadow-md transform scale-105` 
                  : `${type.color} border hover:shadow-md hover:scale-102`
                }
                text-left focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2
              `}
            >
              <div className="flex items-start space-x-4">
                <div className={`
                  p-3 rounded-lg
                  ${isSelected ? 'bg-white' : 'bg-white/50'}
                `}>
                  <Icon className={`h-6 w-6 ${isSelected ? type.color.split(' ')[2] : 'text-neutral-600'}`} />
                </div>
                <div className="flex-1">
                  <h4 className="font-semibold text-base mb-1">{type.title}</h4>
                  <p className="text-sm opacity-80">{type.description}</p>
                </div>
              </div>
              {isSelected && (
                <div className="absolute top-2 right-2">
                  <div className="w-6 h-6 bg-primary-600 rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                </div>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default UserTypeSelector;

