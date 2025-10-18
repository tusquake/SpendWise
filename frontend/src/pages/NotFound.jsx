import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFound() {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 text-gray-800">
            <h1 className="text-6xl font-bold">404</h1>
            <p className="text-xl mt-4">Oops! Page not found.</p>
            <Link
                to="/"
                className="mt-6 px-6 py-3 bg-primary text-white rounded hover:bg-primary/80 transition"
            >
                Go Home
            </Link>
        </div>
    );
}
