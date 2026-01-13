import React, { useEffect, useState } from 'react';
import { bookingAPI } from '../../api/booking';
import type { Booking } from '../../types/api';
import { formatDate } from '../../utils/formatters';

const MyReservations: React.FC = () => {
    const [bookings, setBookings] = useState<Booking[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchBookings();
    }, []);

    const fetchBookings = async () => {
        try {
            setLoading(true);
            const response = await bookingAPI.getByUser();
            setBookings(response.data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load reservations');
        } finally {
            setLoading(false);
        }
    };

    const handlePayment = async (bookingId: string) => {
        try {
            await bookingAPI.processPayment(bookingId);
            fetchBookings(); // Refresh list
        } catch (err: any) {
            alert(err.response?.data?.message || 'Payment failed');
        }
    };

    const handleReturn = async (bookingId: string) => {
        try {
            await bookingAPI.returnCar(bookingId);
            fetchBookings(); // Refresh list
        } catch (err: any) {
            alert(err.response?.data?.message || 'Return failed');
        }
    };

    const handleCancel = async (bookingId: string) => {
        if (!confirm('Are you sure you want to cancel this booking?')) return;

        try {
            await bookingAPI.cancel(bookingId);
            fetchBookings(); // Refresh list
        } catch (err: any) {
            alert(err.response?.data?.message || 'Cancellation failed');
        }
    };

    const getStatusColor = (status: string) => {
        switch (status.toUpperCase()) {
            case 'CONFIRMED': return 'bg-green-100 text-green-800';
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'ACTIVE': return 'bg-blue-100 text-blue-800';
            case 'COMPLETED': return 'bg-gray-100 text-gray-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <div className="animate-pulse text-lg">Loading your reservations...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="max-w-7xl mx-auto px-4 py-8">
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
                    {error}
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold text-neutral-900 mb-8">My Reservations</h1>

            {bookings.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-neutral-600 text-lg">You have no reservations yet.</p>
                    <a href="/cars" className="mt-4 inline-block text-primary-600 hover:text-primary-700 font-medium">
                        Browse available cars
                    </a>
                </div>
            ) : (
                <div className="space-y-4">
                    {bookings.map((booking) => (
                        (() => {
                            const status = (booking.status ?? '').toString().toUpperCase();

                            return (
                        <div key={booking.id} className="bg-white rounded-lg shadow-md p-6 border border-neutral-200">
                            <div className="flex justify-between items-start">
                                <div className="flex-1">
                                    <div className="flex items-center gap-3 mb-2">
                                        <h3 className="text-xl font-semibold text-neutral-900">
                                            {booking.vehicle?.make} {booking.vehicle?.model}
                                        </h3>
                                        <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(status)}`}>
                                            {status}
                                        </span>
                                    </div>

                                    <div className="grid grid-cols-2 gap-4 mt-4 text-sm">
                                        <div>
                                            <span className="text-neutral-600">Pickup:</span>
                                            <p className="font-medium text-neutral-900">{booking.pickupLocation}</p>
                                            <p className="text-neutral-600">{formatDate(booking.startDate)}</p>
                                        </div>
                                        <div>
                                            <span className="text-neutral-600">Dropoff:</span>
                                            <p className="font-medium text-neutral-900">{booking.dropoffLocation}</p>
                                            <p className="text-neutral-600">{formatDate(booking.endDate)}</p>
                                        </div>
                                    </div>

                                    <div className="mt-4">
                                        <span className="text-2xl font-bold text-primary-600">${booking.totalPrice}</span>
                                        <span className="text-neutral-600 ml-2">total</span>
                                    </div>
                                </div>

                                <div className="flex flex-col gap-2 ml-4">
                                    {status === 'PENDING' && (
                                        <>
                                            <button
                                                onClick={() => handlePayment(booking.id.toString())}
                                                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 font-medium"
                                            >
                                                Pay Now
                                            </button>
                                            <button
                                                onClick={() => handleCancel(booking.id.toString())}
                                                className="px-4 py-2 bg-neutral-200 text-neutral-700 rounded-lg hover:bg-neutral-300 font-medium"
                                            >
                                                Cancel
                                            </button>
                                        </>
                                    )}

                                    {status === 'ACTIVE' && (
                                        <button
                                            onClick={() => handleReturn(booking.id.toString())}
                                            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium"
                                        >
                                            Return Car
                                        </button>
                                    )}

                                    {status === 'COMPLETED' && (
                                        <a
                                            href={`/reviews/new?bookingId=${booking.id}`}
                                            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium text-center"
                                        >
                                            Leave Review
                                        </a>
                                    )}
                                </div>
                            </div>
                        </div>
                            );
                        })()
                    ))}
                </div>
            )}
        </div>
    );
};

export default MyReservations;
