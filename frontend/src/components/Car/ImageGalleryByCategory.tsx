import React, { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { vehicleImageAPI } from '../../api/vehicleImage';
import type { VehicleImageResponse, ImageCategory } from '../../types/vehicleImage';
import { IMAGE_CATEGORIES } from '../../types/vehicleImage';

interface ImageGalleryByCategoryProps {
  vehicleId: number;
  layout?: 'grid' | 'carousel' | 'tabs';
  maxImagesPerCategory?: number;
}

export const ImageGalleryByCategory: React.FC<ImageGalleryByCategoryProps> = ({
  vehicleId,
  layout = 'tabs',
  maxImagesPerCategory = 6,
}) => {
  const [mainImage, setMainImage] = useState<VehicleImageResponse | null>(null);
  const [exteriorImages, setExteriorImages] = useState<VehicleImageResponse[]>([]);
  const [interiorImages, setInteriorImages] = useState<VehicleImageResponse[]>([]);
  const [featureImages, setFeatureImages] = useState<VehicleImageResponse[]>([]);
  const [safetyImages, setSafetyImages] = useState<VehicleImageResponse[]>([]);
  const [amenityImages, setAmenityImages] = useState<VehicleImageResponse[]>([]);
  const [performanceImages, setPerformanceImages] = useState<VehicleImageResponse[]>([]);
  const [selectedTab, setSelectedTab] = useState<ImageCategory>('EXTERIOR');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadImages = async () => {
      try {
        setLoading(true);
        setError(null);

        // Load main image
        const main = await vehicleImageAPI.getMainImage(vehicleId);
        if (main) setMainImage(main);

        // Load all category images
        const [ext, int, feat, saf, amen, perf] = await Promise.all([
          vehicleImageAPI.getImagesByCategory(vehicleId, 'EXTERIOR'),
          vehicleImageAPI.getImagesByCategory(vehicleId, 'INTERIOR'),
          vehicleImageAPI.getImagesByCategory(vehicleId, 'FEATURES'),
          vehicleImageAPI.getImagesByCategory(vehicleId, 'SAFETY'),
          vehicleImageAPI.getImagesByCategory(vehicleId, 'AMENITIES'),
          vehicleImageAPI.getImagesByCategory(vehicleId, 'PERFORMANCE'),
        ]);

        setExteriorImages(ext);
        setInteriorImages(int);
        setFeatureImages(feat);
        setSafetyImages(saf);
        setAmenityImages(amen);
        setPerformanceImages(perf);
      } catch (err) {
        setError('Failed to load vehicle images');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadImages();
  }, [vehicleId]);

  const getCategoryImages = (category: ImageCategory): VehicleImageResponse[] => {
    switch (category) {
      case 'EXTERIOR':
        return exteriorImages;
      case 'INTERIOR':
        return interiorImages;
      case 'FEATURES':
        return featureImages;
      case 'SAFETY':
        return safetyImages;
      case 'AMENITIES':
        return amenityImages;
      case 'PERFORMANCE':
        return performanceImages;
      default:
        return [];
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96 bg-neutral-100 rounded-lg">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-primary-500 mx-auto mb-2"></div>
          <p>Loading images...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-96 bg-red-50 rounded-lg border border-red-200">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  // Tabs Layout
  if (layout === 'tabs') {
    const categories: ImageCategory[] = [
      'EXTERIOR',
      'INTERIOR',
      'FEATURES',
      'SAFETY',
      'AMENITIES',
      'PERFORMANCE',
    ];

    const selectedImages = getCategoryImages(selectedTab);

    return (
      <div className="w-full">
        {/* Main Image */}
        {mainImage && (
          <div className="mb-6">
            <img
              src={mainImage.imageUrl}
              alt={mainImage.altText || 'Main vehicle image'}
              className="w-full h-96 object-cover rounded-lg shadow-lg"
              title={mainImage.description}
            />
          </div>
        )}

        {/* Category Tabs */}
        <div className="mb-4 flex gap-2 overflow-x-auto pb-2">
          {categories.map((category) => {
            const images = getCategoryImages(category);
            const hasImages = images.length > 0;

            return (
              <button
                key={category}
                onClick={() => setSelectedTab(category)}
                disabled={!hasImages}
                className={`px-4 py-2 rounded-lg whitespace-nowrap transition-colors ${
                  selectedTab === category
                    ? 'bg-primary-500 text-white'
                    : hasImages
                    ? 'bg-neutral-200 text-neutral-800 hover:bg-neutral-300'
                    : 'bg-neutral-100 text-neutral-400 cursor-not-allowed'
                }`}
              >
                {IMAGE_CATEGORIES[category]} ({images.length})
              </button>
            );
          })}
        </div>

        {/* Selected Category Images Grid */}
        {selectedImages.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {selectedImages.slice(0, maxImagesPerCategory).map((image) => (
              <div
                key={image.id}
                className="relative group overflow-hidden rounded-lg bg-neutral-100 aspect-square"
              >
                <img
                  src={image.imageUrl}
                  alt={image.altText}
                  className="w-full h-full object-cover group-hover:scale-110 transition-transform"
                  title={image.description}
                />
                {image.description && (
                  <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <p className="text-white text-xs">{image.description}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-neutral-500">
            <p>No images available for this category</p>
          </div>
        )}
      </div>
    );
  }

  // Grid Layout
  if (layout === 'grid') {
    return (
      <div className="w-full">
        {mainImage && (
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-2">Main Image</h3>
            <img
              src={mainImage.imageUrl}
              alt={mainImage.altText || 'Main vehicle image'}
              className="w-full h-96 object-cover rounded-lg shadow-lg mb-4"
            />
          </div>
        )}

        {[
          { category: 'EXTERIOR' as ImageCategory, images: exteriorImages },
          { category: 'INTERIOR' as ImageCategory, images: interiorImages },
          { category: 'FEATURES' as ImageCategory, images: featureImages },
          { category: 'SAFETY' as ImageCategory, images: safetyImages },
          { category: 'AMENITIES' as ImageCategory, images: amenityImages },
          { category: 'PERFORMANCE' as ImageCategory, images: performanceImages },
        ].map(({ category, images }) => (
          images.length > 0 && (
            <div key={category} className="mb-8">
              <h3 className="text-lg font-semibold mb-3">
                {IMAGE_CATEGORIES[category]}
              </h3>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {images.slice(0, maxImagesPerCategory).map((image) => (
                  <div
                    key={image.id}
                    className="relative group overflow-hidden rounded-lg bg-neutral-100 aspect-square"
                  >
                    <img
                      src={image.imageUrl}
                      alt={image.altText}
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform"
                      title={image.description}
                    />
                  </div>
                ))}
              </div>
            </div>
          )
        ))}
      </div>
    );
  }

  return null;
};

interface ImageCarouselProps {
  images: VehicleImageResponse[];
  title: string;
}

export const ImageCarousel: React.FC<ImageCarouselProps> = ({ images, title }) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (images.length === 0) return null;

  const goToPrevious = () => {
    setCurrentIndex((prev) => (prev === 0 ? images.length - 1 : prev - 1));
  };

  const goToNext = () => {
    setCurrentIndex((prev) => (prev === images.length - 1 ? 0 : prev + 1));
  };

  const currentImage = images[currentIndex];

  return (
    <div className="mb-8">
      <h3 className="text-lg font-semibold mb-3">{title}</h3>
      <div className="relative bg-neutral-100 rounded-lg overflow-hidden aspect-video">
        <img
          src={currentImage.imageUrl}
          alt={currentImage.altText}
          className="w-full h-full object-cover"
        />

        {images.length > 1 && (
          <>
            <button
              onClick={goToPrevious}
              className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full transition-colors"
              aria-label="Previous image"
            >
              <ChevronLeft size={20} />
            </button>
            <button
              onClick={goToNext}
              className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full transition-colors"
              aria-label="Next image"
            >
              <ChevronRight size={20} />
            </button>

            {/* Image counter */}
            <div className="absolute bottom-2 right-2 bg-black/60 text-white px-3 py-1 rounded-full text-sm">
              {currentIndex + 1} / {images.length}
            </div>

            {/* Thumbnail indicators */}
            <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
              {images.map((_, idx) => (
                <button
                  key={idx}
                  onClick={() => setCurrentIndex(idx)}
                  className={`w-2 h-2 rounded-full transition-colors ${
                    idx === currentIndex ? 'bg-white' : 'bg-white/50'
                  }`}
                  aria-label={`Go to image ${idx + 1}`}
                />
              ))}
            </div>
          </>
        )}
      </div>

      {currentImage.description && (
        <p className="text-sm text-neutral-600 mt-2">{currentImage.description}</p>
      )}
    </div>
  );
};

export default ImageGalleryByCategory;
