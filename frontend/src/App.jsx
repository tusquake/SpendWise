import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import NotFound from './pages/NotFound';
import OAuth2Redirect from './pages/OAuth2Redirect';
import PaymentCallback from './pages/PaymentCallback';
import UpgradePlan from './pages/UpgradePlan';

function App() {
  const { user } = useAuth();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={!user ? <Login /> : <Navigate to="/dashboard" />} />
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
        <Route
          path="/upgrade"
          element={user ? <UpgradePlan /> : <Navigate to="/login" />}
        />
        <Route
          path="/payment/callback"
          element={<PaymentCallback />}
        />
        <Route path="/dashboard" element={user ? <Dashboard /> : <Navigate to="/login" />} />
        <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
