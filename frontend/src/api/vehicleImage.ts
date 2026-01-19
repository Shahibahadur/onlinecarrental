import axiosInstance from './axios';
import type { 
  VehicleImageResponse, 
  VehicleImageRequest, 
  ImageCategory 
} from '../types/vehicleImage';

const API_BASE = '/vehicle-images';

export const vehicleImageAPI = {
  /**
   * Get all images for a vehicle
   */
  getVehicleImages: async (vehicleId: number) => {
    const response = await axiosInstance.get<VehicleImageResponse[]>(
      `${API_BASE}/vehicle/${vehicleId}`
    );
    return response.data;
  },

  /**
   * Get images for a vehicle by category
   */
  getImagesByCategory: async (vehicleId: number, category: ImageCategory) => {
    const response = await axiosInstance.get<VehicleImageResponse[]>(
      `${API_BASE}/vehicle/${vehicleId}/category/${category}`
    );
    return response.data;
  },

  /**
   * Get main image for a vehicle
   */
  getMainImage: async (vehicleId: number) => {
    try {
      const response = await axiosInstance.get<VehicleImageResponse>(
        `${API_BASE}/vehicle/${vehicleId}/main`
      );
      return response.data;
    } catch (error) {
      return null;
    }
  },

  /**
   * Add a new image for a vehicle
   */
  addVehicleImage: async (request: VehicleImageRequest) => {
    const response = await axiosInstance.post<VehicleImageResponse>(
      API_BASE,
      request
    );
    return response.data;
  },

  /**
   * Update image metadata
   */
  updateVehicleImage: async (imageId: number, request: Partial<VehicleImageRequest>) => {
    const response = await axiosInstance.put<VehicleImageResponse>(
      `${API_BASE}/${imageId}`,
      request
    );
    return response.data;
  },

  /**
   * Delete an image
   */
  deleteVehicleImage: async (imageId: number) => {
    await axiosInstance.delete(`${API_BASE}/${imageId}`);
  },

  /**
   * Delete all images in a category for a vehicle
   */
  deleteImagesByCategory: async (vehicleId: number, category: ImageCategory) => {
    await axiosInstance.delete(
      `${API_BASE}/vehicle/${vehicleId}/category/${category}`
    );
  },

  /**
   * Set main image for a vehicle
   */
  setMainImage: async (vehicleId: number, imageId: number) => {
    await axiosInstance.post(
      `${API_BASE}/vehicle/${vehicleId}/set-main/${imageId}`
    );
  },

  /**
   * Reorder images within a category
   */
  reorderImages: async (vehicleId: number, category: ImageCategory, imageIds: number[]) => {
    await axiosInstance.post(
      `${API_BASE}/vehicle/${vehicleId}/category/${category}/reorder`,
      imageIds
    );
  },

  /**
   * Upload a vehicle image
   */
  uploadVehicleImage: async (file: File, category?: string) => {
    const formData = new FormData();
    formData.append('file', file);
    if (category) {
      formData.append('category', category);
    }
    
    const response = await axiosInstance.post<string>(
      '/images/vehicles/upload',
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );
    return response.data;
  },
};
