import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { walletApi, ratesApi } from '../services/api';
import './DashboardPage.css';

function DashboardPage() {
  const [wallets, setWallets] = useState([]);
  const [rates, setRates] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showTopUp, setShowTopUp] = useState(null);
  const [showWithdraw, setShowWithdraw] = useState(null);
  const [showCreateWallet, setShowCreateWallet] = useState(false);
  const [topUpAmount, setTopUpAmount] = useState('');
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [bankAccountNumber, setBankAccountNumber] = useState('');
  const [bankName, setBankName] = useState('');
  const [newCurrency, setNewCurrency] = useState('EUR');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [walletsRes, ratesRes] = await Promise.all([
        walletApi.getWallets(),
        ratesApi.getRates()
      ]);
      setWallets(walletsRes.data);
      setRates(ratesRes.data);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleTopUp = async (walletId) => {
    if (!topUpAmount || parseFloat(topUpAmount) <= 0) return;
    
    setActionLoading(true);
    try {
      await walletApi.topUp(walletId, parseFloat(topUpAmount), 'Top-up');
      await loadData();
      setShowTopUp(null);
      setTopUpAmount('');
      setSuccess('Wallet topped up successfully!');
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError('Failed to top up wallet');
    } finally {
      setActionLoading(false);
    }
  };

  const handleWithdraw = async (walletId) => {
    if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
      setError('Please enter a valid amount');
      return;
    }
    if (!bankAccountNumber || !bankName) {
      setError('Please fill in bank details');
      return;
    }
    
    setActionLoading(true);
    try {
      await walletApi.withdraw(
        walletId, 
        parseFloat(withdrawAmount), 
        bankAccountNumber, 
        bankName, 
        'Bank withdrawal'
      );
      await loadData();
      setShowWithdraw(null);
      setWithdrawAmount('');
      setBankAccountNumber('');
      setBankName('');
      setSuccess(`Successfully withdrew funds to ${bankName}!`);
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to withdraw. Check balance and daily limit.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreateWallet = async () => {
    setActionLoading(true);
    try {
      await walletApi.createWallet(newCurrency);
      await loadData();
      setShowCreateWallet(false);
      setNewCurrency('EUR');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create wallet');
    } finally {
      setActionLoading(false);
    }
  };

  const getCurrencyClass = (currency) => {
    switch (currency) {
      case 'USD': return 'currency-usd';
      case 'EUR': return 'currency-eur';
      case 'GBP': return 'currency-gbp';
      default: return '';
    }
  };

  const formatCurrency = (amount, symbol) => {
    return `${symbol}${parseFloat(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatRate = (rate) => {
    return parseFloat(rate).toFixed(4);
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

  return (
    <div className="page dashboard-page">
      <div className="container">
        <div className="dashboard-header">
          <div>
            <h1>Your Wallets</h1>
            <p className="text-muted">Manage your digital wallets and balances</p>
          </div>
          <div className="dashboard-actions">
            <button 
              className="btn btn-secondary" 
              onClick={() => setShowCreateWallet(true)}
            >
              + Add Wallet
            </button>
            <Link to="/transfer" className="btn btn-primary">
              Transfer Money
            </Link>
          </div>
        </div>

        {error && (
          <div className="auth-error mb-lg">
            {error}
            <button onClick={() => setError('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', color: 'inherit', cursor: 'pointer' }}>×</button>
          </div>
        )}

        {success && (
          <div className="success-message mb-lg">
            {success}
            <button onClick={() => setSuccess('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', color: 'inherit', cursor: 'pointer' }}>×</button>
          </div>
        )}

        {rates && (
          <div className="rates-card animate-fade-in">
            <div className="rates-header">
              <h3>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="20" height="20">
                  <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
                  <polyline points="17 6 23 6 23 12"/>
                </svg>
                Live Exchange Rates
              </h3>
              <span className={`rates-source ${rates.source === 'LIVE' ? 'live' : 'static'}`}>
                {rates.source === 'LIVE' ? '● Live' : '○ Static'}
              </span>
            </div>
            <div className="rates-grid">
              {rates.rates && Object.entries(rates.rates).map(([pair, rate]) => (
                <div key={pair} className="rate-item">
                  <span className="rate-pair">{pair}</span>
                  <span className="rate-value">{formatRate(rate)}</span>
                </div>
              ))}
            </div>
            {rates.lastUpdated && (
              <div className="rates-updated">
                Updated: {new Date(rates.lastUpdated).toLocaleTimeString()}
              </div>
            )}
          </div>
        )}

        <div className="wallets-grid">
          {wallets.map((wallet) => (
            <div key={wallet.id} className="wallet-card animate-fade-in">
              <div className="wallet-header">
                <div className={`wallet-currency ${getCurrencyClass(wallet.currency)}`}>
                  {wallet.currency}
                </div>
                <span className="badge badge-success">Active</span>
              </div>
              
              <div className="wallet-balance">
                <span className="wallet-symbol">{wallet.currencySymbol}</span>
                <span className="wallet-amount">
                  {parseFloat(wallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>

              <div className="wallet-limits">
                <div className="limit-row">
                  <span className="text-muted">Daily Limit</span>
                  <span>{formatCurrency(wallet.dailyLimit, wallet.currencySymbol)}</span>
                </div>
                <div className="limit-row">
                  <span className="text-muted">Remaining Today</span>
                  <span className="text-success">{formatCurrency(wallet.remainingDailyLimit, wallet.currencySymbol)}</span>
                </div>
                <div className="limit-bar">
                  <div 
                    className="limit-bar-fill" 
                    style={{ 
                      width: `${Math.min(100, (parseFloat(wallet.spentToday) / parseFloat(wallet.dailyLimit)) * 100)}%` 
                    }}
                  ></div>
                </div>
              </div>

              <div className="wallet-actions">
                <button 
                  className="btn btn-secondary"
                  onClick={() => setShowTopUp(wallet.id)}
                >
                  Top Up
                </button>
                <button 
                  className="btn btn-outline-danger"
                  onClick={() => setShowWithdraw(wallet.id)}
                >
                  Withdraw
                </button>
              </div>
            </div>
          ))}
        </div>

        {wallets.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">💳</div>
            <h3>No Wallets Yet</h3>
            <p className="text-muted">Create your first wallet to get started</p>
            <button 
              className="btn btn-primary mt-md"
              onClick={() => setShowCreateWallet(true)}
            >
              Create Wallet
            </button>
          </div>
        )}

        {showTopUp && (
          <div className="modal-overlay" onClick={() => { setShowTopUp(null); setTopUpAmount(''); }}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <h3>Top Up Wallet</h3>
              <p className="text-muted mb-lg">Add funds to your wallet</p>
              
              <div className="form-group">
                <label>Amount</label>
                <input
                  type="number"
                  placeholder="Enter amount"
                  value={topUpAmount}
                  onChange={(e) => setTopUpAmount(e.target.value)}
                  min="0.01"
                  step="0.01"
                />
              </div>

              <div className="modal-actions">
                <button 
                  className="btn btn-secondary"
                  onClick={() => { setShowTopUp(null); setTopUpAmount(''); }}
                >
                  Cancel
                </button>
                <button 
                  className="btn btn-primary"
                  onClick={() => handleTopUp(showTopUp)}
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Processing...' : 'Add Funds'}
                </button>
              </div>
            </div>
          </div>
        )}

        {showWithdraw && (
          <div className="modal-overlay" onClick={() => { setShowWithdraw(null); setWithdrawAmount(''); setBankAccountNumber(''); setBankName(''); }}>
            <div className="modal withdraw-modal" onClick={(e) => e.stopPropagation()}>
              <h3>Withdraw to Bank</h3>
              <p className="text-muted mb-lg">Transfer funds to your bank account</p>
              
              <div className="form-group">
                <label>Amount</label>
                <input
                  type="number"
                  placeholder="Enter amount"
                  value={withdrawAmount}
                  onChange={(e) => setWithdrawAmount(e.target.value)}
                  min="0.01"
                  step="0.01"
                />
              </div>

              <div className="form-group">
                <label>Bank Name</label>
                <input
                  type="text"
                  placeholder="e.g., Chase Bank"
                  value={bankName}
                  onChange={(e) => setBankName(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Account Number</label>
                <input
                  type="text"
                  placeholder="Enter account number"
                  value={bankAccountNumber}
                  onChange={(e) => setBankAccountNumber(e.target.value)}
                />
              </div>

              <div className="modal-actions">
                <button 
                  className="btn btn-secondary"
                  onClick={() => { setShowWithdraw(null); setWithdrawAmount(''); setBankAccountNumber(''); setBankName(''); }}
                >
                  Cancel
                </button>
                <button 
                  className="btn btn-danger"
                  onClick={() => handleWithdraw(showWithdraw)}
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Processing...' : 'Withdraw'}
                </button>
              </div>
            </div>
          </div>
        )}

        {showCreateWallet && (
          <div className="modal-overlay" onClick={() => setShowCreateWallet(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <h3>Create New Wallet</h3>
              <p className="text-muted mb-lg">Select a currency for your new wallet</p>
              
              <div className="form-group">
                <label>Currency</label>
                <select 
                  value={newCurrency} 
                  onChange={(e) => setNewCurrency(e.target.value)}
                >
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                </select>
              </div>

              <div className="modal-actions">
                <button 
                  className="btn btn-secondary"
                  onClick={() => setShowCreateWallet(false)}
                >
                  Cancel
                </button>
                <button 
                  className="btn btn-primary"
                  onClick={handleCreateWallet}
                  disabled={actionLoading}
                >
                  {actionLoading ? 'Creating...' : 'Create Wallet'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default DashboardPage;
