import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Trash2, Upload, Eye, X, ChevronDown, ChevronUp } from 'lucide-react';
import { adminAPI } from '../../api/admin';
import type { VehicleImage, AdminImageListResponse } from '../../api/admin';

const VehicleImagesManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedVehicle, setSelectedVehicle] = useState<number | null>(null);
  const [uploadCategory, setUploadCategory] = useState<string>('');
  const [expandedVehicles, setExpandedVehicles] = useState<Set<number>>(new Set());

  const { data: imageData, isLoading, error } = useQuery({
    queryKey: ['adminVehicleImages'],
    queryFn: async () => {
      const response = await adminAPI.listVehicleImages();
      return response.data;
    },
  });

  // Fetch filesystem-based categories (folders under uploads/vehicles)
  const { data: fsCategoriesMap } = useQuery({
    queryKey: ['imageCategoriesFilesystem'],
    queryFn: async () => {
      const response = await adminAPI.listImageCategories();
      return response.data;
    },
  });

  // Fetch DB-backed categories (admin-managed categories)
  const { data: dbCategories } = useQuery({
    queryKey: ['imageCategoriesDB'],
    queryFn: async () => {
      const response = await adminAPI.getImageCategories();
      return response.data;
    },
  });

  const createCategoryMutation = useMutation({
    mutationFn: (payload: { name: string; description?: string }) => adminAPI.createImageCategory(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['imageCategoriesDB'] });
    },
  });

  // Build unified category list (merge filesystem and DB names)
  const fsCategoryList = fsCategoriesMap ? Object.keys(fsCategoriesMap) : [];
  const dbCategoryList = dbCategories ? dbCategories.map((c) => String(c.name)) : [];
  const categorySet = new Set<string>([...fsCategoryList.map((c) => c.toLowerCase()), ...dbCategoryList.map((c) => c.toLowerCase())]);
  const categoryList = Array.from(categorySet).sort();

  const deleteMutation = useMutation({
    mutationFn: (imageId: number) => adminAPI.deleteVehicleImage(imageId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicleImages'] });
    },
  });

  const uploadMutation = useMutation({
    mutationFn: ({ vehicleId, category, file }: { vehicleId: number; category: string; file: File }) =>
      adminAPI.uploadVehicleImageForVehicle(vehicleId, category, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminVehicleImages'] });
    },
  });

  const handleFileUpload = (vehicleId: number, category: string, file: File) => {
    uploadMutation.mutate({ vehicleId, category, file });
  };

  const handleDelete = (imageId: number) => {
    if (confirm('Are you sure you want to delete this image?')) {
      deleteMutation.mutate(imageId);
    }
  };

  const toggleExpanded = (vehicleId: number) => {
    const newExpanded = new Set(expandedVehicles);
    if (newExpanded.has(vehicleId)) {
      newExpanded.delete(vehicleId);
    } else {
      newExpanded.add(vehicleId);
    }
    setExpandedVehicles(newExpanded);
  };

  const groupedImages = imageData?.reduce((acc, item) => {
    acc[item.vehicleId] = item;
    return acc;
  }, {} as Record<number, AdminImageListResponse>) || {};

  if (isLoading) return <div className="p-4">Loading...</div>;
  if (error) return <div className="p-4 text-red-500">Error loading images</div>;

  return (
    <div className="p-6">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-neutral-900 mb-2">Vehicle Images Management</h2>
        <p className="text-neutral-600">Manage images for all vehicles</p>

        {/* Categories panel */}
        <div className="mt-4 bg-white p-4 rounded-lg border">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium">Categories</h3>
            <div className="flex items-center gap-2">
              <input
                type="text"
                placeholder="Add category (e.g., accessories)"
                value={uploadCategory}
                onChange={(e) => setUploadCategory(e.target.value)}
                className="px-3 py-2 border border-neutral-300 rounded-md"
              />
              <button
                onClick={() => {
                  const name = uploadCategory?.trim();
                  if (!name) return;
                  createCategoryMutation.mutate({ name: name.toUpperCase(), description: '' });
                }}
                className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                Add
              </button>
            </div>
          </div>

          <div className="mt-3 grid grid-cols-2 md:grid-cols-4 gap-2">
            {categoryList.map((cat) => (
              <div key={cat} className="p-2 bg-neutral-50 rounded-md text-sm flex items-center justify-between">
                <span className="truncate">{cat}</span>
                <button
                  onClick={() => setUploadCategory(cat)}
                  className="ml-2 text-xs text-blue-600 hover:underline"
                >
                  Use
                </button>
              </div>
            ))}
            {categoryList.length === 0 && <div className="text-sm text-neutral-500">No categories found</div>}
          </div>
        </div>
      </div>

      <div className="space-y-4">
        {Object.values(groupedImages).map((vehicleData) => (
          <div key={vehicleData.vehicleId} className="bg-white rounded-lg shadow-sm border">
            <div
              className="p-4 cursor-pointer flex items-center justify-between hover:bg-neutral-50"
              onClick={() => toggleExpanded(vehicleData.vehicleId)}
            >
              <div>
                <h3 className="text-lg font-semibold text-neutral-900">{vehicleData.vehicleInfo}</h3>
                <p className="text-sm text-neutral-600">{vehicleData.images.length} images</p>
              </div>
              {expandedVehicles.has(vehicleData.vehicleId) ? <ChevronUp /> : <ChevronDown />}
            </div>

            {expandedVehicles.has(vehicleData.vehicleId) && (
              <div className="p-4 border-t">
                <div className="mb-4">
                  <label className="block text-sm font-medium text-neutral-700 mb-2">Upload New Image</label>
                  <div className="flex gap-2">
                    <select
                      value={uploadCategory}
                      onChange={(e) => setUploadCategory(e.target.value)}
                      className="px-3 py-2 border border-neutral-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="">Select Category</option>
                      {categoryList.map((cat) => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        if (file && uploadCategory) {
                          handleFileUpload(vehicleData.vehicleId, uploadCategory, file);
                        }
                      }}
                      className="px-3 py-2 border border-neutral-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {vehicleData.images.map((image) => (
                    <div key={image.id} className="relative group">
                      <img
                        src={image.imageUrl}
                        alt={image.altText || image.imageName}
                        className="w-full h-48 object-cover rounded-lg"
                      />
                      <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all rounded-lg flex items-center justify-center">
                        <div className="opacity-0 group-hover:opacity-100 flex gap-2">
                          <button
                            onClick={() => window.open(image.imageUrl, '_blank')}
                            className="p-2 bg-white rounded-full hover:bg-neutral-200"
                          >
                            <Eye className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(image.id)}
                            className="p-2 bg-red-500 text-white rounded-full hover:bg-red-600"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                      <div className="mt-2">
                        <p className="text-sm font-medium">{image.category}</p>
                        <p className="text-xs text-neutral-500">{image.imageName}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default VehicleImagesManagement;