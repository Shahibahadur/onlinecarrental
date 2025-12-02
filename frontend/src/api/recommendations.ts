import axiosInstance from './axios';
import { Car } from '../types/car';

export const recommendationsAPI = {
  getRecommendations: (limit: number = 5) =>
    axiosInstance.get<Car[]>('/recommendations', { params: { limit } }),
  
  getPersonalizedRecommendations: () =>
    axiosInstance.get<Car[]>('/recommendations/personalized'),
  
  getPopularCars: () =>
    axiosInstance.get<Car[]>('/recommendations/popular'),
};
