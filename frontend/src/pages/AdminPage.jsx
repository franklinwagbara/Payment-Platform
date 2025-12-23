import { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import './AdminPage.css';

function AdminPage() {
  const [analytics, setAnalytics] = useState(null);
  const [users, setUsers] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    try {
      const [analyticsRes, usersRes, transactionsRes] = await Promise.all([
        adminApi.getAnalytics(),
        adminApi.getUsers(0, 10),
        adminApi.getTransactions(0, 10)
      ]);
      setAnalytics(analyticsRes.data);
      setUsers(usersRes.data.content || []);
      setTransactions(transactionsRes.data.content || []);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('Access denied. Admin privileges required.');
      } else {
        setError('Failed to load admin data');
      }
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return `$${parseFloat(amount || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  };

  const formatDate = (date) => {
    return new Date(date).toLocaleString();
  };

  if (loading) {
    return (
      <div className="page">
        <div className="container flex items-center justify-center" style={{ minHeight: '50vh' }}>
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page admin-page">
        <div className="container">
          <div className="admin-error">
            <div className="error-icon">🔒</div>
            <h2>Access Denied</h2>
            <p>{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page admin-page">
      <div className="container">
        <div className="admin-header">
          <h1>Admin Dashboard</h1>
          <p className="text-muted">System overview and management</p>
        </div>

        {analytics && (
          <div className="stats-grid animate-fade-in">
            <div className="stat-card">
              <div className="stat-icon users">👥</div>
              <div className="stat-content">
                <span className="stat-value">{analytics.totalUsers}</span>
                <span className="stat-label">Total Users</span>
                <span className="stat-detail">{analytics.activeUsers} active</span>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon wallets">💳</div>
              <div className="stat-content">
                <span className="stat-value">{analytics.totalWallets}</span>
                <span className="stat-label">Total Wallets</span>
                <span className="stat-detail">{formatCurrency(analytics.totalBalanceAllWallets)} total balance</span>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon transactions">📊</div>
              <div className="stat-content">
                <span className="stat-value">{analytics.totalTransactions}</span>
                <span className="stat-label">Transactions</span>
                <span className="stat-detail">{analytics.transactionsLast24Hours} in last 24h</span>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon success">✓</div>
              <div className="stat-content">
                <span className="stat-value">{analytics.completedTransactions}</span>
                <span className="stat-label">Completed</span>
                <span className="stat-detail text-danger">{analytics.failedTransactions} failed</span>
              </div>
            </div>
          </div>
        )}

        {analytics?.walletsByCurrency && (
          <div className="currency-distribution animate-fade-in">
            <h3>Wallet Distribution by Currency</h3>
            <div className="currency-bars">
              {Object.entries(analytics.walletsByCurrency).map(([currency, count]) => (
                <div key={currency} className="currency-bar-item">
                  <span className="currency-name">{currency}</span>
                  <div className="bar-container">
                    <div 
                      className={`bar-fill ${currency.toLowerCase()}`}
                      style={{ width: `${(count / analytics.totalWallets) * 100}%` }}
                    ></div>
                  </div>
                  <span className="currency-count">{count}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="admin-tabs">
          <button 
            className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            Users
          </button>
          <button 
            className={`tab-btn ${activeTab === 'transactions' ? 'active' : ''}`}
            onClick={() => setActiveTab('transactions')}
          >
            Recent Transactions
          </button>
        </div>

        {activeTab === 'users' && (
          <div className="admin-table-container animate-fade-in">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Email</th>
                  <th>Name</th>
                  <th>Role</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.email}</td>
                    <td>{user.fullName}</td>
                    <td>
                      <span className={`badge ${user.role === 'ADMIN' ? 'badge-primary' : 'badge-secondary'}`}>
                        {user.role}
                      </span>
                    </td>
                    <td>
                      <span className={`badge ${user.active ? 'badge-success' : 'badge-danger'}`}>
                        {user.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'transactions' && (
          <div className="admin-table-container animate-fade-in">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td>{formatDate(tx.createdAt)}</td>
                    <td>{tx.type}</td>
                    <td>
                      <span className="amount">${parseFloat(tx.amount).toFixed(2)}</span>
                    </td>
                    <td>
                      <span className={`badge ${tx.status === 'COMPLETED' ? 'badge-success' : 'badge-danger'}`}>
                        {tx.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default AdminPage;
