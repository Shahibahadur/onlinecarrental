import axiosInstance from './axios';
import type { VehicleType } from '../types/carTypeImages';

export const carTypeImageAPI = {
  /**
   * Upload a vehicle image organized by car type
   */
  uploadVehicleImage: async (file: File, vehicleType: VehicleType) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('vehicleType', vehicleType);
    
    const response = await axiosInstance.post<string>(
      '/images/vehicles/upload',
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );
    return response.data;
  },

  /**
   * Get image URL for display
   */
  getImageUrl: (vehicleType: VehicleType, filename: string) => {
    return `/api/images/vehicles/${vehicleType.toLowerCase()}/${filename}`;
  },

  /**
   * Get image URL from the database imageNames field
   * imageNames format: "image1.jpg,image2.jpg,image3.jpg"
   */
  parseImageNames: (imageNames: string | undefined, vehicleType: VehicleType): string[] => {
    if (!imageNames) return [];
    return imageNames
      .split(',')
      .map(name => name.trim())
      .filter(name => name.length > 0)
      .map(filename => carTypeImageAPI.getImageUrl(vehicleType, filename));
  },

  /**
   * Format image names for database storage
   */
  formatImageNames: (filenames: string[]): string => {
    return filenames.join(',');
  },

  /**
   * Add a new image name to existing imageNames string
   */
  addImageName: (existingImageNames: string | undefined, newFilename: string): string => {
    const names = existingImageNames ? existingImageNames.split(',').map(n => n.trim()) : [];
    if (!names.includes(newFilename)) {
      names.push(newFilename);
    }
    return names.join(',');
  },

  /**
   * Remove an image name from imageNames string
   */
  removeImageName: (existingImageNames: string, filenameToRemove: string): string => {
    return existingImageNames
      .split(',')
      .map(n => n.trim())
      .filter(name => name !== filenameToRemove)
      .join(',');
  },
};
