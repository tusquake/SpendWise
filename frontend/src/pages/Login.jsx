import { IndianRupee } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { login, register } = useAuth();
    const [isLogin, setIsLogin] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
    });

    // Handle OAuth2 redirect with tokens
    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');
        const refreshToken = params.get('refreshToken');

        if (token && refreshToken) {
            // Store tokens
            localStorage.setItem('token', token);
            localStorage.setItem('refreshToken', refreshToken);

            // Navigate to dashboard
            navigate('/dashboard');
        }
    }, [location, navigate]);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            let result;
            if (isLogin) {
                result = await login(formData.email, formData.password);
            } else {
                result = await register(formData.name, formData.email, formData.password);
            }

            if (result.success) {
                navigate('/dashboard');
            } else {
                setError(result.message || 'Authentication failed');
            }
        } catch (err) {
            setError('An error occurred. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleOAuth2Login = (provider) => {
        const backendUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
        window.location.href = `${backendUrl}/oauth2/authorization/${provider}`;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
                {/* Logo & Title */}
                <div className="text-center mb-8">
                    <div className="bg-blue-600 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                        <IndianRupee className="text-white" size={32} />
                    </div>
                    <h1 className="text-3xl font-bold text-gray-800">SpendWise</h1>
                    <p className="text-gray-600 mt-2">
                        Intelligent financial insights powered by AI
                    </p>
                </div>

                {/* Error Message */}
                {error && (
                    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
                        {error}
                    </div>
                )}

                {/* OAuth2 Buttons */}
                <div className="mb-6">
                    <button
                        type="button"
                        onClick={() => handleOAuth2Login('google')}
                        className="w-full flex items-center justify-center gap-3 bg-white border-2 border-gray-300 text-gray-700 py-3 rounded-lg font-semibold hover:bg-gray-50 transition mb-3"
                    >
                        <svg className="w-5 h-5" viewBox="0 0 24 24">
                            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                        </svg>
                        Continue with Google
                    </button>

                    <button
                        type="button"
                        onClick={() => handleOAuth2Login('github')}
                        className="w-full flex items-center justify-center gap-3 bg-gray-800 text-white py-3 rounded-lg font-semibold hover:bg-gray-900 transition"
                    >
                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                            <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
                        </svg>
                        Continue with GitHub
                    </button>
                </div>

                {/* Divider */}
                <div className="relative mb-6">
                    <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-300"></div>
                    </div>
                    <div className="relative flex justify-center text-sm">
                        <span className="px-2 bg-white text-gray-500">Or continue with email</span>
                    </div>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit}>
                    <h2 className="text-2xl font-semibold mb-6 text-gray-800">
                        {isLogin ? 'Login' : 'Register'}
                    </h2>

                    {/* Name Field (Register Only) */}
                    {!isLogin && (
                        <div className="mb-4">
                            <label className="block text-gray-700 mb-2 font-medium">
                                Full Name
                            </label>
                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Tushar Seth"
                                required={!isLogin}
                            />
                        </div>
                    )}

                    {/* Email Field */}
                    <div className="mb-4">
                        <label className="block text-gray-700 mb-2 font-medium">Email</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="tusharseth@example.com"
                            required
                        />
                    </div>

                    {/* Password Field */}
                    <div className="mb-6">
                        <label className="block text-gray-700 mb-2 font-medium">
                            Password
                        </label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="••••••••"
                            required
                            minLength={6}
                        />
                        {!isLogin && (
                            <p className="text-sm text-gray-500 mt-1">
                                Minimum 6 characters
                            </p>
                        )}
                    </div>

                    {/* Submit Button */}
                    <button
                        type="submit"
                        disabled={loading}
                        className={`w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition ${loading ? 'opacity-50 cursor-not-allowed' : ''
                            }`}
                    >
                        {loading ? 'Please wait...' : isLogin ? 'Login' : 'Register'}
                    </button>

                    {/* Toggle Login/Register */}
                    <p className="text-center mt-4 text-gray-600">
                        {isLogin ? "Don't have an account? " : 'Already have an account? '}
                        <button
                            type="button"
                            onClick={() => {
                                setIsLogin(!isLogin);
                                setError('');
                                setFormData({ name: '', email: '', password: '' });
                            }}
                            className="text-blue-600 font-semibold hover:underline"
                        >
                            {isLogin ? 'Register' : 'Login'}
                        </button>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default Login;