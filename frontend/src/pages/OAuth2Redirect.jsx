import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const OAuth2Redirect = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { oauthLogin } = useAuth();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const token = params.get('token');
        const refreshToken = params.get('refreshToken');
        const error = params.get('error');

        if (error) {
            console.error('OAuth2 error:', error);
            navigate('/login', {
                state: { error: 'Authentication failed. Please try again.' },
            });
            return;
        }

        if (token && refreshToken) {
            // ✅ Update AuthContext + localStorage
            oauthLogin(token, refreshToken);

            // ✅ Redirect to dashboard
            navigate('/dashboard', { replace: true });
        } else {
            navigate('/login');
        }
    }, [location, navigate, oauthLogin]);

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
            <div className="text-center">
                <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600 text-lg">Completing authentication...</p>
            </div>
        </div>
    );
};

export default OAuth2Redirect;
