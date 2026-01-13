import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Star, User, Car, Search, Trash2, Eye } from 'lucide-react';
import { reviewAPI, type Review } from '../../api/review';
import { adminAPI, type PaginatedResponse } from '../../api/admin';

const FeedbackManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedReview, setSelectedReview] = useState<Review | null>(null);

  // Note: We need to fetch all reviews. Since there's no admin endpoint for all reviews,
  // we'll need to create one or fetch from multiple vehicles. For now, we'll use a placeholder.
  // In a real implementation, you'd add GET /api/admin/reviews endpoint.

  const { data: reviewsData, isLoading, error } = useQuery({
    queryKey: ['adminReviews', page, pageSize, searchTerm],
    queryFn: async () => {
      const response = await adminAPI.getAllReviews(page, pageSize);
      return response.data;
    },
  });

  const deleteReviewMutation = useMutation({
    mutationFn: (reviewId: string) => adminAPI.deleteReview(reviewId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminReviews'] });
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
      alert('Review deleted successfully');
    },
    onError: (error: any) => {
      alert(error?.response?.data?.message || 'Failed to delete review');
    },
  });

  const reviews = reviewsData?.content || [];
  const totalPages = reviewsData?.totalPages || 0;
  const totalElements = reviewsData?.totalElements || 0;

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleDelete = (reviewId: string) => {
    if (window.confirm('Are you sure you want to delete this review?')) {
      deleteReviewMutation.mutate(reviewId);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-neutral-900">Feedback Management</h2>
          <p className="text-neutral-600">View and manage customer reviews and feedback</p>
        </div>
      </div>

      {/* Search */}
      <div className="bg-white rounded-lg shadow-md p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-neutral-400" />
          <input
            type="text"
            placeholder="Search reviews by user name, vehicle name, or comment..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      {/* Reviews Table */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            <p className="mt-4 text-neutral-600">Loading reviews...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">
            Failed to load reviews. Please try again.
          </div>
        ) : reviews.length === 0 ? (
          <div className="p-8 text-center text-neutral-600">
            <Star className="h-12 w-12 text-neutral-300 mx-auto mb-4" />
            <p>No reviews found.</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-neutral-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      User
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Vehicle
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Rating
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Comment
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Date
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-neutral-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-neutral-200">
                  {reviews.map((review: Review) => (
                    <tr key={review.id} className="hover:bg-neutral-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <User className="h-4 w-4 text-neutral-400 mr-2" />
                          <div>
                            <div className="text-sm font-medium text-neutral-900">
                              {review.userName}
                            </div>
                            <div className="text-sm text-neutral-500">ID: {review.userId}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <Car className="h-4 w-4 text-neutral-400 mr-2" />
                          <div className="text-sm text-neutral-900">Vehicle ID: {review.vehicleId}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
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
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm text-neutral-900 max-w-md truncate">
                          {review.comment}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-neutral-600">
                        {formatDate(review.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex items-center space-x-2">
                          <button
                            onClick={() => setSelectedReview(review)}
                            className="px-3 py-1 bg-blue-600 text-white rounded text-xs font-medium hover:bg-blue-700 transition-colors flex items-center gap-1"
                            title="View Details"
                          >
                            <Eye className="h-3 w-3" />
                            View
                          </button>
                          <button
                            onClick={() => handleDelete(review.id)}
                            disabled={deleteReviewMutation.isPending}
                            className="px-3 py-1 bg-red-600 text-white rounded text-xs font-medium hover:bg-red-700 transition-colors flex items-center gap-1 disabled:opacity-50 disabled:cursor-not-allowed"
                            title="Delete Review"
                          >
                            <Trash2 className="h-3 w-3" />
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="bg-neutral-50 px-6 py-4 flex items-center justify-between border-t border-neutral-200">
                <div className="text-sm text-neutral-600">
                  Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} reviews
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="px-4 py-2 border border-neutral-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-neutral-50"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="px-4 py-2 border border-neutral-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-neutral-50"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Review Detail Modal */}
      {selectedReview && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-neutral-200">
              <div className="flex items-center justify-between">
                <h3 className="text-xl font-bold text-neutral-900">Review Details</h3>
                <button
                  onClick={() => setSelectedReview(null)}
                  className="text-neutral-400 hover:text-neutral-600 transition-colors"
                >
                  ✕
                </button>
              </div>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="text-sm font-medium text-neutral-600">User</label>
                <p className="text-neutral-900 font-semibold">{selectedReview.userName}</p>
                <p className="text-sm text-neutral-500">User ID: {selectedReview.userId}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-neutral-600">Vehicle</label>
                <p className="text-neutral-900">Vehicle ID: {selectedReview.vehicleId}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-neutral-600">Rating</label>
                <div className="flex items-center gap-1 mt-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      className={`h-5 w-5 ${
                        star <= selectedReview.rating
                          ? 'text-yellow-400 fill-yellow-400'
                          : 'text-neutral-300'
                      }`}
                    />
                  ))}
                  <span className="ml-2 font-medium text-neutral-700">
                    {selectedReview.rating}/5
                  </span>
                </div>
              </div>
              <div>
                <label className="text-sm font-medium text-neutral-600">Comment</label>
                <p className="text-neutral-900 mt-1 whitespace-pre-wrap">{selectedReview.comment}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-neutral-600">Date</label>
                <p className="text-neutral-900 mt-1">{formatDate(selectedReview.createdAt)}</p>
              </div>
            </div>
            <div className="p-6 border-t border-neutral-200 flex justify-end gap-3">
              <button
                onClick={() => setSelectedReview(null)}
                className="px-4 py-2 border border-neutral-300 text-neutral-700 rounded-lg font-medium hover:bg-neutral-50 transition-colors"
              >
                Close
              </button>
              <button
                onClick={() => {
                  handleDelete(selectedReview.id);
                  setSelectedReview(null);
                }}
                className="px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 transition-colors"
              >
                Delete Review
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FeedbackManagement;
