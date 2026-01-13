import axiosInstance from './axios';

export interface Review {
  id: string;
  userId: string;
  vehicleId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReviewRequest {
  vehicleId: string;
  rating: number;
  comment: string;
}

export interface PaginatedReviewResponse {
  content: Review[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const reviewAPI = {
  create: (data: ReviewRequest) =>
    axiosInstance.post<Review>('/reviews', data),
  
  getById: (id: string) =>
    axiosInstance.get<Review>(`/reviews/${id}`),
  
  getByVehicle: (vehicleId: string, page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedReviewResponse>(`/reviews/vehicle/${vehicleId}`, {
      params: { page, size },
    }),
  
  getByUser: (userId: string, page: number = 0, size: number = 10) =>
    axiosInstance.get<PaginatedReviewResponse>(`/reviews/user/${userId}`, {
      params: { page, size },
    }),
  
  update: (id: string, data: ReviewRequest) =>
    axiosInstance.put<Review>(`/reviews/${id}`, data),
  
  delete: (id: string) =>
    axiosInstance.delete(`/reviews/${id}`),
  
  getAverageRating: (vehicleId: string) =>
    axiosInstance.get<number>(`/reviews/vehicle/${vehicleId}/average-rating`),
};
