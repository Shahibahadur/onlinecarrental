import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { paymentAPI } from '../../api/payment';

type EsewaParams = Record<string, string>;

export default function EsewaCheckout() {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const bookingIdStr = params.get('bookingId');
  const bookingId = bookingIdStr ? Number(bookingIdStr) : undefined;

  const [esewaParams, setEsewaParams] = useState<EsewaParams | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initiate = async () => {
      try {
        if (!bookingId) {
          setError('Missing bookingId');
          return;
        }

        // amount is ignored by backend when bookingId is provided
        const resp = await paymentAPI.esewaInitiate({ bookingId, amount: 0 });
        setEsewaParams(resp.data);
      } catch (e: any) {
        setError(e?.response?.data?.message || e?.message || 'Failed to initiate eSewa payment');
      } finally {
        setLoading(false);
      }
    };

    initiate();
  }, [bookingId]);

  if (loading) return <div className="text-center py-10">Preparing eSewa checkout...</div>;
  if (error) return <div className="text-center py-10 text-red-600">{error}</div>;

  if (!esewaParams) return <div className="text-center py-10">Could not load eSewa payment options.</div>;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-neutral-900 mb-6 text-center">Pay with eSewa</h1>
      <div className="max-w-xl mx-auto bg-white rounded-lg shadow-md p-8">
        <p className="text-neutral-600 mb-6">You will be redirected to eSewa to complete payment securely.</p>
        <form action="https://rc-epay.esewa.com.np/api/epay/main/v2/form" method="POST">
          {Object.keys(esewaParams).map((k) => (
            <input key={k} type="hidden" name={k} value={esewaParams[k]} />
          ))}
          <button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-4 rounded-lg transition-colors"
          >
            Pay with eSewa
          </button>
        </form>
      </div>
    </div>
  );
}
