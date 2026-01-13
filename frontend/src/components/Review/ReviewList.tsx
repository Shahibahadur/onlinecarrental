import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Star, User } from 'lucide-react';
import { reviewAPI, type Review } from '../../api/review';

interface ReviewListProps {
  vehicleId: string;
  pageSize?: number;
}

const ReviewList: React.FC<ReviewListProps> = ({ vehicleId, pageSize = 10 }) => {
  const { data, isLoading, error } = useQuery({
    queryKey: ['reviews', vehicleId, 0, pageSize],
    queryFn: async () => {
      const response = await reviewAPI.getByVehicle(vehicleId, 0, pageSize);
      return response.data;
    },
  });

  const reviews = data?.content || [];

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (isLoading) {
    return (
      <div className="text-center py-8">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p className="mt-4 text-neutral-600">Loading reviews...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8 text-red-600">
        Failed to load reviews. Please try again.
      </div>
    );
  }

  if (reviews.length === 0) {
    return (
      <div className="text-center py-8">
        <Star className="h-12 w-12 text-neutral-300 mx-auto mb-4" />
        <p className="text-neutral-600">No reviews yet. Be the first to review this vehicle!</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-neutral-900">
          Reviews ({data?.totalElements || 0})
        </h3>
      </div>

      <div className="space-y-4">
        {reviews.map((review: Review) => (
          <div key={review.id} className="bg-white border border-neutral-200 rounded-lg p-6">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 bg-primary-100 rounded-full flex items-center justify-center">
                  <User className="h-5 w-5 text-primary-600" />
                </div>
                <div>
                  <p className="font-semibold text-neutral-900">{review.userName}</p>
                  <p className="text-sm text-neutral-500">{formatDate(review.createdAt)}</p>
                </div>
              </div>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Star
                    key={star}
                    className={`h-4 w-4 ${
                      star <= review.rating
                        ? 'text-yellow-400 fill-yellow-400'
                        : 'text-neutral-300'
                    }`}
                  />
                ))}
                <span className="ml-2 text-sm font-medium text-neutral-700">
                  {review.rating}/5
                </span>
              </div>
            </div>
            <p className="text-neutral-700 whitespace-pre-wrap">{review.comment}</p>
          </div>
        ))}
      </div>

      {data && data.totalElements > pageSize && (
        <div className="text-center pt-4">
          <p className="text-sm text-neutral-600">
            Showing {reviews.length} of {data.totalElements} reviews
          </p>
        </div>
      )}
    </div>
  );
};

export default ReviewList;
