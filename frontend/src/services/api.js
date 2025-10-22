import axiosInstance from '../utils/axiosInstance';

export const authAPI = {
    login: (email, password) =>
        axiosInstance.post('/auth/login', { email, password }),

    register: (name, email, password) =>
        axiosInstance.post('/auth/register', { name, email, password }),
};

export const transactionAPI = {
    getAll: () => axiosInstance.get('/transactions/all'),
    add: (data) => axiosInstance.post('/transactions/add', data),
    delete: (id) => axiosInstance.delete(`/transactions/${id}`),
};

export const aiAPI = {
    getInsights: () => axiosInstance.get('/ai/insights'),
    chat: (query) => axiosInstance.post('/ai/chatbot', { query }),
};

export const paymentAPI = {
    // Get subscription plans
    getPlans: () => axiosInstance.get('/payments/plans'),

    // Create payment order (returns checkout URL or Razorpay details)
    createOrder: (data) => axiosInstance.post('/payments/create-order', data),

    // Verify payment after user completes checkout
    verifyPayment: (data) => axiosInstance.post('/payments/verify', data),

    // Get payment history
    getPaymentHistory: () => axiosInstance.get('/payments/history'),
};
