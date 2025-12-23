import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './contexts/AuthContext';
import Navbar from './components/common/Navbar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import HistoryPage from './pages/HistoryPage';
import TransferPage from './pages/TransferPage';
import AdminPage from './pages/AdminPage';
import LedgerVerificationPage from './pages/LedgerVerificationPage';

function PrivateRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="flex items-center justify-center" style={{ height: '100vh' }}>
        <div className="spinner"></div>
      </div>
    );
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
}

function PublicRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="flex items-center justify-center" style={{ height: '100vh' }}>
        <div className="spinner"></div>
      </div>
    );
  }
  
  return !isAuthenticated ? children : <Navigate to="/dashboard" />;
}

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <>
      {isAuthenticated && <Navbar />}
      <Routes>
        <Route path="/login" element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        } />
        <Route path="/register" element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        } />
        <Route path="/dashboard" element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        } />
        <Route path="/history" element={
          <PrivateRoute>
            <HistoryPage />
          </PrivateRoute>
        } />
        <Route path="/transfer" element={
          <PrivateRoute>
            <TransferPage />
          </PrivateRoute>
        } />
        <Route path="/admin" element={
          <PrivateRoute>
            <AdminPage />
          </PrivateRoute>
        } />
        <Route path="/admin/ledger" element={
          <PrivateRoute>
            <LedgerVerificationPage />
          </PrivateRoute>
        } />
        <Route path="/" element={<Navigate to="/dashboard" />} />
        <Route path="*" element={<Navigate to="/dashboard" />} />
      </Routes>
    </>
  );
}

export default App;
