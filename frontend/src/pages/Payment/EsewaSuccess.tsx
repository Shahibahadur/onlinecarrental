import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { paymentAPI } from '../../api/payment';

export default function EsewaSuccess() {
  const location = useLocation();
  const [status, setStatus] = useState<string>('Verifying Payment...');
  const [error, setError] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const verify = async () => {
      const params = new URLSearchParams(location.search);
      const data = params.get('data');

      if (!data) {
        setStatus('Verification Failed');
        setError('No data received from eSewa.');
        setIsLoading(false);
        return;
      }

      try {
        const decoded = JSON.parse(atob(data));
        const uuid = decoded.transaction_uuid;
        const amount = decoded.total_amount;

        if (!uuid || !amount) {
          throw new Error('Invalid eSewa response data');
        }

        const resp = await paymentAPI.esewaVerify(String(uuid), String(amount));
        const s = resp.data?.status;

        if (String(s).toUpperCase() === 'COMPLETE' || String(s).toUpperCase() === 'COMPLETED') {
          setStatus('Payment Verified & Successful!');
        } else {
          setStatus(`Payment Status: ${s}`);
          setError('The payment was not completed successfully.');
        }
      } catch (e: any) {
        setStatus('Verification Failed');
        setError(e?.response?.data?.message || e?.message || 'There was an error verifying payment.');
      } finally {
        setIsLoading(false);
      }
    };

    verify();
  }, [location.search]);

  return (
    <div className="container mx-auto px-4 py-8 text-center">
      <div className="bg-white p-8 rounded-lg shadow-md max-w-md mx-auto">
        <h1 className="text-3xl font-bold text-neutral-900 mb-4">Payment Status</h1>
        {isLoading ? (
          <p className="text-lg font-semibold mb-4">Verifying Payment...</p>
        ) : (
          <>
            <p className="text-lg font-semibold mb-4">{status}</p>
            {error && <p className="text-red-600 mb-6">{error}</p>}
          </>
        )}
        <Link
          to="/my-reservations"
          className="bg-primary-600 hover:bg-primary-700 text-white font-bold py-2 px-4 rounded transition-colors mt-4 inline-block"
        >
          Go to My Reservations
        </Link>
      </div>
    </div>
  );
}
