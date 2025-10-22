import {
    Crown,
    IndianRupee,
    LogOut,
    MessageSquare,
    PieChartIcon,
    Plus,
    Send,
    Trash2,
    TrendingUp,
    X
} from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    CartesianGrid,
    Cell,
    Legend,
    Line,
    LineChart,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import { useAuth } from '../context/AuthContext';
import { aiAPI, transactionAPI } from '../services/api';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [aiInsight, setAiInsight] = useState('');
    const [showAddTransaction, setShowAddTransaction] = useState(false);
    const [showChatbot, setShowChatbot] = useState(false);
    const [chatMessages, setChatMessages] = useState([
        {
            type: 'bot',
            text: "Hello! I'm your AI finance assistant. Ask me anything about your expenses!",
        },
    ]);
    const [chatInput, setChatInput] = useState('');

    const [newTransaction, setNewTransaction] = useState({
        description: '',
        amount: '',
        date: new Date().toISOString().split('T')[0],
        paymentMode: 'UPI',
    });

    const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

    // Fetch transactions on mount
    useEffect(() => {
        fetchTransactions();
        fetchInsights();
    }, []);

    const fetchTransactions = async () => {
        try {
            setLoading(true);
            const response = await transactionAPI.getAll();
            setTransactions(response.data.data || []);
        } catch (error) {
            console.error('Error fetching transactions:', error);
        } finally {
            setLoading(false);
        }
    };
    const [isInsightLoading, setIsInsightLoading] = useState(false);

    const fetchInsights = async () => {
        try {
            setIsInsightLoading(true); // start loading
            const response = await aiAPI.getInsights();
            setAiInsight(response.data.data || '');
        } catch (error) {
            console.error('Error fetching insights:', error);
        } finally {
            setIsInsightLoading(false); // stop loading
        }
    };


    const handleAddTransaction = async (e) => {
        e.preventDefault();
        try {
            await transactionAPI.add({
                ...newTransaction,
                amount: parseFloat(newTransaction.amount),
            });

            setShowAddTransaction(false);
            setNewTransaction({
                description: '',
                amount: '',
                date: new Date().toISOString().split('T')[0],
                paymentMode: 'UPI',
            });

            fetchTransactions();
            fetchInsights();
        } catch (error) {
            console.error('Error adding transaction:', error);
            alert('Failed to add transaction');
        }
    };

    const handleDeleteTransaction = async (id) => {
        if (window.confirm('Are you sure you want to delete this transaction?')) {
            try {
                await transactionAPI.delete(id);
                fetchTransactions();
                fetchInsights();
            } catch (error) {
                console.error('Error deleting transaction:', error);
                alert('Failed to delete transaction');
            }
        }
    };

    const [isLoading, setIsLoading] = useState(false);

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!chatInput.trim()) return;

        const userMessage = { type: 'user', text: chatInput };
        setChatMessages((prev) => [...prev, userMessage]);
        setChatInput('');
        setIsLoading(true);

        try {
            const response = await aiAPI.chat(chatInput);

            if (response.status === 429) {
                // Rate limit exceeded
                const rateLimitMessage = {
                    type: 'bot',
                    text: response.data.message,
                    upgrade: true, // flag to show upgrade button
                };
                setChatMessages((prev) => [...prev, rateLimitMessage]);
            } else if (response.status === 200) {
                const botMessage = { type: 'bot', text: response.data.reply };
                setChatMessages((prev) => [...prev, botMessage]);
            } else {
                const errorMessage = { type: 'bot', text: 'Something went wrong. Please try again.' };
                setChatMessages((prev) => [...prev, errorMessage]);
            }
        } catch (error) {
            if (error.response && error.response.status === 429) {
                const rateLimitMessage = {
                    type: 'bot',
                    text: error.response.data.message,
                    upgrade: true,
                };
                setChatMessages((prev) => [...prev, rateLimitMessage]);
            } else {
                const errorMessage = { type: 'bot', text: 'Unable to connect. Please try again later.' };
                setChatMessages((prev) => [...prev, errorMessage]);
            }
        } finally {
            setIsLoading(false);
        }
    };




    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Calculate analytics
    const totalSpending = transactions.reduce((sum, t) => sum + t.amount, 0);

    const categoryData = transactions.reduce((acc, t) => {
        acc[t.category] = (acc[t.category] || 0) + t.amount;
        return acc;
    }, {});

    const pieData = Object.entries(categoryData).map(([name, value]) => ({
        name,
        value,
    }));

    // Mock monthly data (you can calculate this from actual transactions)
    const monthlyData = [
        { month: 'Jul', amount: 0 },
        { month: 'Aug', amount: 0 },
        { month: 'Sep', amount: 0 },
        { month: 'Oct', amount: totalSpending },
    ];


    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Loading your dashboard...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Navbar */}
            <nav className="bg-white shadow-md">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        <div className="flex items-center">
                            <div className="bg-blue-600 w-10 h-10 rounded-lg flex items-center justify-center">
                                <IndianRupee className="text-white" size={24} />
                            </div>
                            <span className="ml-3 text-xl font-bold text-gray-800">
                                SpendWise
                            </span>
                        </div>

                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-4">
                                {!user?.isPremium && (
                                    <button
                                        onClick={() => navigate('/upgrade')}
                                        className="bg-yellow-500 text-white px-4 py-2 rounded-lg font-semibold hover:bg-yellow-600 transition flex items-center"
                                    >
                                        <Crown size={20} className="mr-2" />
                                        Upgrade to Premium
                                    </button>
                                )}
                                <span className="text-gray-700">Welcome, {user?.name || 'User'}</span>
                                <button
                                    onClick={handleLogout}
                                    className="flex items-center text-gray-600 hover:text-red-600 transition"
                                >
                                    <LogOut size={20} className="mr-1" />
                                    Logout
                                </button>
                            </div>
                            {/* <span className="text-gray-700">
                                Welcome,&nbsp;
                                {user?.name
                                    ? user.name
                                    : user?.email
                                        ? user.email.split('@')[0]
                                        : 'User'}
                            </span>
                            <button
                                onClick={handleLogout}
                                className="flex items-center text-gray-600 hover:text-red-600 transition"
                            >
                                <LogOut size={20} className="mr-1" />
                                Logout
                            </button> */}
                        </div>
                    </div>
                </div>
            </nav>

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* AI Insight Banner */}
                {isInsightLoading ? (
                    <div className="bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl p-6 mb-8 text-white shadow-lg animate-pulse">
                        <div className="flex items-start">
                            <div className="mr-3 mt-1 flex-shrink-0">
                                <div className="w-6 h-6 bg-blue-300 rounded-full"></div>
                            </div>
                            <div className="flex-1 space-y-2">
                                <div className="h-5 w-1/3 bg-blue-300 rounded"></div>
                                <div className="h-4 w-2/3 bg-blue-200 rounded"></div>
                                <div className="h-4 w-1/2 bg-blue-200 rounded"></div>
                            </div>
                        </div>
                    </div>
                ) : aiInsight && (
                    <div className="bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl p-6 mb-8 text-white shadow-lg">
                        <div className="flex items-start">
                            <TrendingUp className="mr-3 mt-1 flex-shrink-0" size={24} />
                            <div>
                                <h3 className="text-lg font-semibold mb-2">AI Financial Insight</h3>
                                <p className="text-blue-50">{aiInsight}</p>
                            </div>
                        </div>
                    </div>

                )}

                {/* Summary Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    <div className="bg-white rounded-xl shadow-md p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-gray-500 text-sm">Total Spending</p>
                                <p className="text-3xl font-bold text-gray-800 mt-1">
                                    ₹{totalSpending.toFixed(2)}
                                </p>
                            </div>
                            <div className="bg-blue-100 p-3 rounded-full">
                                <IndianRupee className="text-blue-600" size={28} />
                            </div>
                        </div>
                    </div>

                    <div className="bg-white rounded-xl shadow-md p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-gray-500 text-sm">Transactions</p>
                                <p className="text-3xl font-bold text-gray-800 mt-1">
                                    {transactions.length}
                                </p>
                            </div>
                            <div className="bg-green-100 p-3 rounded-full">
                                <TrendingUp className="text-green-600" size={28} />
                            </div>
                        </div>
                    </div>

                    <div className="bg-white rounded-xl shadow-md p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-gray-500 text-sm">Categories</p>
                                <p className="text-3xl font-bold text-gray-800 mt-1">
                                    {Object.keys(categoryData).length}
                                </p>
                            </div>
                            <div className="bg-purple-100 p-3 rounded-full">
                                <PieChartIcon className="text-purple-600" size={28} />
                            </div>
                        </div>
                    </div>
                </div>

                {/* Charts */}
                {transactions.length > 0 && (
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                        {/* Pie Chart */}
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <h3 className="text-lg font-semibold text-gray-800 mb-4">
                                Spending by Category
                            </h3>
                            <ResponsiveContainer width="100%" height={300}>
                                <PieChart>
                                    <Pie
                                        data={pieData}
                                        cx="50%"
                                        cy="50%"
                                        labelLine={false}
                                        label={({ name, percent }) =>
                                            `${name} ${(percent * 100).toFixed(0)}%`
                                        }
                                        outerRadius={80}
                                        fill="#8884d8"
                                        dataKey="value"
                                    >
                                        {pieData.map((entry, index) => (
                                            <Cell
                                                key={`cell-${index}`}
                                                fill={COLORS[index % COLORS.length]}
                                            />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>

                        {/* Line Chart */}
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <h3 className="text-lg font-semibold text-gray-800 mb-4">
                                Monthly Trend
                            </h3>
                            <ResponsiveContainer width="100%" height={300}>
                                <LineChart data={monthlyData}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="month" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Line
                                        type="monotone"
                                        dataKey="amount"
                                        stroke="#3b82f6"
                                        strokeWidth={2}
                                    />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                )}

                {/* Transactions Table */}
                <div className="bg-white rounded-xl shadow-md p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-semibold text-gray-800">
                            Recent Transactions
                        </h3>
                        <button
                            onClick={() => setShowAddTransaction(true)}
                            className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700 transition"
                        >
                            <Plus size={20} className="mr-1" />
                            Add Transaction
                        </button>
                    </div>

                    {transactions.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-gray-500 mb-4">No transactions yet</p>
                            <button
                                onClick={() => setShowAddTransaction(true)}
                                className="text-blue-600 hover:underline"
                            >
                                Add your first transaction
                            </button>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                    <tr className="border-b">
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Description
                                        </th>
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Category
                                        </th>
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Amount
                                        </th>
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Date
                                        </th>
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Payment
                                        </th>
                                        <th className="text-left py-3 px-4 text-gray-600 font-semibold">
                                            Action
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {transactions.map((transaction) => (
                                        <tr key={transaction.id} className="border-b hover:bg-gray-50">
                                            <td className="py-3 px-4">{transaction.description}</td>
                                            <td className="py-3 px-4">
                                                <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm">
                                                    {transaction.category || 'Uncategorized'}
                                                </span>
                                            </td>
                                            <td className="py-3 px-4 font-semibold">
                                                ₹{transaction.amount}
                                            </td>
                                            <td className="py-3 px-4">{transaction.date}</td>
                                            <td className="py-3 px-4">{transaction.paymentMode}</td>
                                            <td className="py-3 px-4">
                                                <button
                                                    onClick={() => handleDeleteTransaction(transaction.id)}
                                                    className="text-red-600 hover:text-red-800"
                                                >
                                                    <Trash2 size={18} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Add Transaction Modal */}
            {showAddTransaction && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="text-xl font-semibold text-gray-800">
                                Add Transaction
                            </h3>
                            <button
                                onClick={() => setShowAddTransaction(false)}
                                className="text-gray-500 hover:text-gray-700"
                            >
                                <X size={24} />
                            </button>
                        </div>
                        <form onSubmit={handleAddTransaction}>
                            <div className="mb-4">
                                <label className="block text-gray-700 mb-2">Description</label>
                                <input
                                    type="text"
                                    value={newTransaction.description}
                                    onChange={(e) =>
                                        setNewTransaction({
                                            ...newTransaction,
                                            description: e.target.value,
                                        })
                                    }
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    placeholder="e.g., Uber Ride"
                                    required
                                />
                            </div>
                            <div className="mb-4">
                                <label className="block text-gray-700 mb-2">Amount (₹)</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={newTransaction.amount}
                                    onChange={(e) =>
                                        setNewTransaction({
                                            ...newTransaction,
                                            amount: e.target.value,
                                        })
                                    }
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    placeholder="320"
                                    required
                                />
                            </div>

                            {/* Category with Subcategories */}
                            <div className="mb-6">
                                <label className="block text-gray-700 mb-2">Category</label>
                                <select
                                    value={newTransaction.category}
                                    onChange={(e) =>
                                        setNewTransaction({
                                            ...newTransaction,
                                            category: e.target.value,
                                        })
                                    }
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                >
                                    <option value="">Select Category</option>

                                    {/* Food */}
                                    <optgroup label="Food & Dining">
                                        <option value="Groceries">Groceries</option>
                                        <option value="Restaurants">Restaurants</option>
                                        <option value="Cafes">Cafes</option>
                                        <option value="Snacks">Snacks</option>
                                        <option value="Delivery">Food Delivery</option>
                                    </optgroup>

                                    {/* Travel */}
                                    <optgroup label="Travel & Transport">
                                        <option value="Fuel">Fuel</option>
                                        <option value="Cab">Cab / Taxi</option>
                                        <option value="PublicTransport">Bus / Metro</option>
                                        <option value="Flights">Flights</option>
                                        <option value="HotelStay">Hotel Stay</option>
                                    </optgroup>

                                    {/* Shopping */}
                                    <optgroup label="Shopping & Lifestyle">
                                        <option value="Clothing">Clothing</option>
                                        <option value="Accessories">Accessories</option>
                                        <option value="Electronics">Electronics</option>
                                        <option value="HomeDecor">Home Decor</option>
                                        <option value="Gadgets">Gadgets</option>
                                    </optgroup>

                                    {/* Bills */}
                                    <optgroup label="Bills & Utilities">
                                        <option value="Electricity">Electricity</option>
                                        <option value="Water">Water</option>
                                        <option value="Internet">Internet</option>
                                        <option value="MobileRecharge">Mobile Recharge</option>
                                        <option value="Rent">Rent</option>
                                    </optgroup>

                                    {/* Health */}
                                    <optgroup label="Health & Fitness">
                                        <option value="Doctor">Doctor Visits</option>
                                        <option value="Medicines">Medicines</option>
                                        <option value="Gym">Gym / Fitness</option>
                                        <option value="Insurance">Health Insurance</option>
                                    </optgroup>

                                    {/* Entertainment */}
                                    <optgroup label="Entertainment & Leisure">
                                        <option value="Movies">Movies</option>
                                        <option value="Subscriptions">Streaming Subscriptions</option>
                                        <option value="Games">Games</option>
                                        <option value="Events">Events / Concerts</option>
                                    </optgroup>

                                    {/* Education */}
                                    <optgroup label="Education & Learning">
                                        <option value="Courses">Online Courses</option>
                                        <option value="Books">Books</option>
                                        <option value="Workshops">Workshops</option>
                                        <option value="SchoolFees">School / College Fees</option>
                                    </optgroup>

                                    {/* Others */}
                                    <optgroup label="Others">
                                        <option value="Donations">Donations</option>
                                        <option value="Gifts">Gifts</option>
                                        <option value="Miscellaneous">Miscellaneous</option>
                                    </optgroup>
                                </select>
                            </div>


                            <div className="mb-4">
                                <label className="block text-gray-700 mb-2">Date</label>
                                <input
                                    type="date"
                                    value={newTransaction.date}
                                    onChange={(e) =>
                                        setNewTransaction({
                                            ...newTransaction,
                                            date: e.target.value,
                                        })
                                    }
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    required
                                />
                            </div>
                            <div className="mb-6">
                                <label className="block text-gray-700 mb-2">Payment Mode</label>
                                <select
                                    value={newTransaction.paymentMode}
                                    onChange={(e) =>
                                        setNewTransaction({
                                            ...newTransaction,
                                            paymentMode: e.target.value,
                                        })
                                    }
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                >
                                    <option value="UPI">UPI</option>
                                    <option value="Card">Card</option>
                                    <option value="Cash">Cash</option>
                                    <option value="Bank">Bank Transfer</option>
                                </select>
                            </div>
                            <button
                                type="submit"
                                className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
                            >
                                Add Transaction
                            </button>
                        </form>
                    </div>
                </div>
            )}

            {/* Chatbot Widget */}
            {!showChatbot && (
                <button
                    onClick={() => setShowChatbot(true)}
                    className="fixed bottom-6 right-6 bg-blue-600 text-white p-4 rounded-full shadow-2xl hover:bg-blue-700 transition z-40"
                >
                    <MessageSquare size={28} />
                </button>
            )}

            {showChatbot && (
                <div className="fixed bottom-6 right-6 w-96 bg-white rounded-xl shadow-2xl z-50">
                    {/* Chat Header */}
                    <div className="bg-blue-600 text-white p-4 rounded-t-xl flex justify-between items-center">
                        <div className="flex items-center">
                            <MessageSquare className="mr-2" size={24} />
                            <span className="font-semibold">AI Finance Assistant</span>
                        </div>
                        <button
                            onClick={() => setShowChatbot(false)}
                            className="text-white hover:text-gray-200"
                        >
                            <X size={24} />
                        </button>
                    </div>

                    {/* Chat Messages */}
                    <div className="h-96 overflow-y-auto p-4 space-y-3">
                        {chatMessages.map((msg, idx) => (
                            <div
                                key={idx}
                                className={`flex ${msg.type === 'user' ? 'justify-end' : 'justify-start'}`}
                            >
                                <div
                                    className={`max-w-xs px-4 py-2 rounded-lg ${msg.type === 'user'
                                        ? 'bg-blue-600 text-white'
                                        : 'bg-gray-200 text-gray-800'
                                        }`}
                                >
                                    <p>{msg.text}</p>

                                    {/* Show upgrade button if rate-limit exceeded */}
                                    {msg.upgrade && (
                                        <button
                                            onClick={() => navigate('/upgrade')}
                                            className="mt-2 bg-yellow-500 text-white px-3 py-1 rounded-lg font-semibold hover:bg-yellow-600 transition flex items-center"
                                        >
                                            <Crown size={16} className="mr-1" />
                                            Upgrade to Premium
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}

                        {/* Busy Indicator */}
                        {isLoading && (
                            <div className="flex justify-start">
                                <div className="flex items-center space-x-2 bg-gray-100 px-3 py-2 rounded-lg text-gray-600">
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.2s]"></div>
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:-0.4s]"></div>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Input Box */}
                    <form onSubmit={handleSendMessage} className="p-4 border-t">
                        <div className="flex space-x-2">
                            <input
                                type="text"
                                value={chatInput}
                                onChange={(e) => setChatInput(e.target.value)}
                                placeholder="Ask about your expenses..."
                                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                disabled={isLoading}
                            />
                            <button
                                type="submit"
                                disabled={isLoading}
                                className={`bg-blue-600 text-white p-2 rounded-lg transition ${isLoading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-700'
                                    }`}
                            >
                                <Send size={20} />
                            </button>
                        </div>
                    </form>
                </div>
            )}


        </div>
    );
};

export default Dashboard;