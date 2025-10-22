import { Check, CreditCard, Crown, Smartphone } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { paymentAPI } from '../services/api';

const UpgradePlan = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [plans, setPlans] = useState([]);
    const [selectedPlan, setSelectedPlan] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showPaymentModal, setShowPaymentModal] = useState(false);

    useEffect(() => {
        fetchPlans();
    }, []);

    const fetchPlans = async () => {
        try {
            const response = await paymentAPI.getPlans();
            setPlans(response.data.data);
        } catch (error) {
            console.error('Error fetching plans:', error);
        }
    };

    const handleSelectPlan = (plan) => {
        if (plan.name === 'FREE') return;
        setSelectedPlan(plan);
        setShowPaymentModal(true);
    };

    const handlePayment = async (paymentMethod) => {
        setLoading(true);

        try {
            const response = await paymentAPI.createOrder({
                amount: selectedPlan.monthlyPrice,
                paymentMethod: paymentMethod,
                subscriptionTier: selectedPlan.name,
                durationMonths: 1,
            });

            const paymentData = response.data.data;

            if (paymentMethod === 'UPI' && paymentData.checkoutUrl) {
                window.location.href = paymentData.checkoutUrl;
            } else if (paymentData.razorpayKeyId) {
                openRazorpayCheckout(paymentData);
            }
        } catch (error) {
            console.error('Payment failed:', error);
            alert('Payment initiation failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const openRazorpayCheckout = (paymentData) => {
        const options = {
            key: paymentData.razorpayKeyId,
            amount: paymentData.amount * 100,
            currency: 'INR',
            name: 'AI Expense Tracker',
            description: `${selectedPlan.name} Subscription`,
            order_id: paymentData.orderId,
            handler: async function (response) {
                try {
                    await paymentAPI.verifyPayment({
                        orderId: response.razorpay_order_id,
                        paymentId: response.razorpay_payment_id,
                        signature: response.razorpay_signature,
                    });

                    alert('Payment successful! Your subscription has been upgraded.');
                    navigate('/dashboard');
                } catch (error) {
                    alert('Payment verification failed.');
                }
            },
            prefill: {
                name: user.name,
                email: user.email,
            },
            theme: {
                color: '#3b82f6',
            },
            modal: {
                ondismiss: function () {
                    setLoading(false);
                }
            }
        };

        const rzp = new window.Razorpay(options);
        rzp.open();
    };

    return (
        <div className="min-h-screen bg-gray-50 py-12 px-4">
            <div className="max-w-7xl mx-auto">
                {/* Back Button */}
                <button
                    onClick={() => navigate('/dashboard')}
                    className="mb-6 flex items-center text-blue-600 hover:text-blue-800 font-semibold"
                >
                    &#8592; Back to Dashboard
                </button>

                <div className="text-center mb-12">
                    <h1 className="text-4xl font-bold text-gray-900 mb-4">
                        Choose Your Plan
                    </h1>
                    <p className="text-xl text-gray-600">
                        Upgrade to Premium for unlimited AI insights
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {plans.map((plan) => (
                        <div
                            key={plan.name}
                            className={`bg-white rounded-2xl shadow-lg p-8 relative ${plan.badge ? 'border-4 border-blue-500' : 'border border-gray-200'
                                }`}
                        >
                            {plan.badge && (
                                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                                    <span className="bg-blue-500 text-white px-4 py-1 rounded-full text-sm font-semibold">
                                        {plan.badge}
                                    </span>
                                </div>
                            )}

                            <div className="text-center mb-6">
                                {plan.name === 'PREMIUM' && (
                                    <Crown className="w-16 h-16 text-yellow-500 mx-auto mb-4" />
                                )}
                                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                                    {plan.name}
                                </h2>
                                <div className="text-4xl font-bold text-gray-900 mb-2">
                                    ₹{plan.monthlyPrice}
                                    <span className="text-lg text-gray-500">/month</span>
                                </div>
                            </div>

                            <ul className="space-y-4 mb-8">
                                {plan.features.split(',').map((feature, index) => (
                                    <li key={index} className="flex items-start">
                                        <Check className="w-5 h-5 text-green-500 mr-2 flex-shrink-0 mt-0.5" />
                                        <span className="text-gray-600">{feature.trim()}</span>
                                    </li>
                                ))}
                            </ul>

                            <button
                                onClick={() => handleSelectPlan(plan)}
                                disabled={plan.name === 'FREE' || loading}
                                className={`w-full py-3 rounded-lg font-semibold transition ${plan.name === 'FREE'
                                    ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                                    : plan.name === 'PREMIUM'
                                        ? 'bg-blue-600 text-white hover:bg-blue-700'
                                        : 'bg-gray-800 text-white hover:bg-gray-900'
                                    }`}
                            >
                                {plan.name === 'FREE' ? 'Current Plan' : 'Upgrade Now'}
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Payment Method Modal */}
            {showPaymentModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
                        <h3 className="text-2xl font-bold text-gray-900 mb-6">
                            Choose Payment Method
                        </h3>

                        <div className="space-y-4">
                            <button
                                onClick={() => handlePayment('UPI')}
                                disabled={loading}
                                className="w-full flex items-center justify-between p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition"
                            >
                                <div className="flex items-center">
                                    <Smartphone className="w-6 h-6 text-blue-600 mr-3" />
                                    <div className="text-left">
                                        <p className="font-semibold text-gray-900">UPI Payment</p>
                                        <p className="text-sm text-gray-500">
                                            PhonePe, GPay, Paytm
                                        </p>
                                    </div>
                                </div>
                                <span className="text-green-600 font-semibold">Recommended</span>
                            </button>

                            <button
                                onClick={() => handlePayment('DEBIT_CARD')}
                                disabled={loading}
                                className="w-full flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition"
                            >
                                <CreditCard className="w-6 h-6 text-purple-600 mr-3" />
                                <div className="text-left">
                                    <p className="font-semibold text-gray-900">Debit/Credit Card</p>
                                    <p className="text-sm text-gray-500">
                                        Visa, Mastercard, Rupay
                                    </p>
                                </div>
                            </button>

                            <button
                                onClick={() => handlePayment('NET_BANKING')}
                                disabled={loading}
                                className="w-full flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition"
                            >
                                <CreditCard className="w-6 h-6 text-orange-600 mr-3" />
                                <div className="text-left">
                                    <p className="font-semibold text-gray-900">Net Banking</p>
                                    <p className="text-sm text-gray-500">All major banks</p>
                                </div>
                            </button>
                        </div>

                        <button
                            onClick={() => setShowPaymentModal(false)}
                            className="w-full mt-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition"
                        >
                            Cancel
                        </button>

                        {loading && (
                            <div className="mt-4 text-center">
                                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-500 border-t-transparent"></div>
                                <p className="mt-2 text-gray-600">Redirecting to payment...</p>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default UpgradePlan;
