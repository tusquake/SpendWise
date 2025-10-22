import React, { createContext, useContext, useState } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);

    // Login function
    const login = async (email, password) => {
        try {
            const response = await authAPI.login(email, password);
            const { token, user: userData } = response.data.data;

            localStorage.setItem('accessToken', token);
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(userData);

            return { success: true };
        } catch (error) {
            return { success: false, message: error.response?.data?.message || 'Login failed' };
        }
    };

    // Register function
    const register = async (name, email, password) => {
        try {
            const response = await authAPI.register(name, email, password);
            const { token, user: userData } = response.data.data;

            localStorage.setItem('accessToken', token);
            localStorage.setItem('user', JSON.stringify(userData));
            setUser(userData);

            return { success: true };
        } catch (error) {
            return { success: false, message: error.response?.data?.message || 'Registration failed' };
        }
    };

    // Logout function
    const logout = () => {
        localStorage.clear();
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);