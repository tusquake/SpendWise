import { CheckCircle, Loader, XCircle } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const PaymentCallback = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('processing');
    const [message, setMessage] = useState('Processing your payment...');

    useEffect(() => {
        handleCallback();
    }, []);

    const handleCallback = async () => {
        const paymentStatus = searchParams.get('status');
        const orderId = searchParams.get('orderId');

        if (paymentStatus === 'success') {
            setStatus('success');
            setMessage('Payment successful! Your subscription has been upgraded.');
            setTimeout(() => navigate('/dashboard'), 3000);
        } else {
            setStatus('failed');
            setMessage('Payment failed. Please try again.');
            setTimeout(() => navigate('/upgrade'), 3000);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full text-center">
                {status === 'processing' && (
                    <>
                        <Loader className="w-16 h-16 text-blue-600 mx-auto mb-4 animate-spin" />
                        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                            Processing Payment
                        </h2>
                        <p className="text-gray-600">{message}</p>
                    </>
                )}

                {status === 'success' && (
                    <>
                        <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
                        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                            Payment Successful!
                        </h2>
                        <p className="text-gray-600 mb-4">{message}</p>
                        <p className="text-sm text-gray-500">
                            Redirecting to dashboard in 3 seconds...
                        </p>
                    </>
                )}

                {status === 'failed' && (
                    <>
                        <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
                        <h2 className="text-2xl font-semibold text-gray-900 mb-2">
                            Payment Failed
                        </h2>
                        <p className="text-gray-600 mb-4">{message}</p>
                        <button
                            onClick={() => navigate('/upgrade')}
                            className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
                        >
                            Try Again
                        </button>
                    </>
                )}
            </div>
        </div>
    );
};

export default PaymentCallback;