import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { walletApi, transactionApi, userApi } from '../services/api';
import './TransferPage.css';

function TransferPage() {
  const [wallets, setWallets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [transferMode, setTransferMode] = useState('self');
  
  const [recipientEmail, setRecipientEmail] = useState('');
  const [recipientInfo, setRecipientInfo] = useState(null);
  const [lookingUp, setLookingUp] = useState(false);
  
  const [formData, setFormData] = useState({
    sourceWalletId: '',
    targetWalletId: '',
    amount: '',
    description: ''
  });

  const navigate = useNavigate();

  useEffect(() => {
    loadWallets();
  }, []);

  const loadWallets = async () => {
    try {
      const response = await walletApi.getWallets();
      setWallets(response.data);
      if (response.data.length > 0) {
        setFormData(prev => ({ ...prev, sourceWalletId: response.data[0].id }));
      }
    } catch (err) {
      setError('Failed to load wallets');
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = (mode) => {
    setTransferMode(mode);
    setRecipientEmail('');
    setRecipientInfo(null);
    setFormData(prev => ({ ...prev, targetWalletId: '' }));
    setError('');
    setSuccess('');
  };

  const handleLookupRecipient = async () => {
    if (!recipientEmail.trim()) {
      setError('Please enter an email address');
      return;
    }

    setLookingUp(true);
    setError('');
    setRecipientInfo(null);

    try {
      const response = await userApi.lookupRecipient(recipientEmail.trim());
      setRecipientInfo(response.data);
      if (response.data.wallets.length > 0) {
        setFormData(prev => ({ ...prev, targetWalletId: response.data.wallets[0].id }));
      }
    } catch (err) {
      if (err.response?.status === 404) {
        setError('User not found. Please check the email address.');
      } else if (err.response?.status === 400) {
        setError('You cannot send money to yourself. Use "My Wallets" mode instead.');
      } else {
        setError('Failed to find recipient. Please try again.');
      }
    } finally {
      setLookingUp(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!formData.sourceWalletId || !formData.targetWalletId) {
      setError('Please select both source and target wallets');
      return;
    }

    if (formData.sourceWalletId === formData.targetWalletId) {
      setError('Source and target wallets must be different');
      return;
    }

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError('Please enter a valid amount');
      return;
    }

    setSubmitting(true);

    try {
      await transactionApi.transfer(
        formData.sourceWalletId,
        formData.targetWalletId,
        parseFloat(formData.amount),
        formData.description || 'Transfer'
      );

      const recipientName = transferMode === 'other' && recipientInfo 
        ? ` to ${recipientInfo.firstName}` 
        : '';
      setSuccess(`Transfer completed successfully${recipientName}!`);
      setFormData(prev => ({ ...prev, amount: '', description: '' }));
      
      setTimeout(() => {
        navigate('/dashboard');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const getWalletDisplay = (wallet) => {
    return `${wallet.currency} - ${wallet.currencySymbol}${parseFloat(wallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  };

  const sourceWallet = wallets.find(w => w.id === formData.sourceWalletId);

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
    <div className="page transfer-page">
      <div className="container">
        <div className="transfer-container animate-fade-in">
          <div className="transfer-header">
            <h1>Transfer Money</h1>
            <p className="text-muted">Send money between wallets or to another user</p>
          </div>

          <div className="transfer-mode-toggle">
            <button
              type="button"
              className={`mode-btn ${transferMode === 'self' ? 'active' : ''}`}
              onClick={() => handleModeChange('self')}
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="5" width="20" height="14" rx="2"/>
                <line x1="2" y1="10" x2="22" y2="10"/>
              </svg>
              My Wallets
            </button>
            <button
              type="button"
              className={`mode-btn ${transferMode === 'other' ? 'active' : ''}`}
              onClick={() => handleModeChange('other')}
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <line x1="19" y1="8" x2="19" y2="14"/>
                <line x1="22" y1="11" x2="16" y2="11"/>
              </svg>
              Another User
            </button>
          </div>

          <form onSubmit={handleSubmit} className="transfer-form">
            {error && <div className="auth-error">{error}</div>}
            {success && <div className="transfer-success">{success}</div>}

            <div className="form-group">
              <label htmlFor="sourceWalletId">From Wallet</label>
              <select
                id="sourceWalletId"
                name="sourceWalletId"
                value={formData.sourceWalletId}
                onChange={handleChange}
                required
              >
                <option value="">Select source wallet</option>
                {wallets.map(wallet => (
                  <option key={wallet.id} value={wallet.id}>
                    {getWalletDisplay(wallet)}
                  </option>
                ))}
              </select>
              {sourceWallet && (
                <div className="wallet-info">
                  <span className="text-muted">Available: </span>
                  <span className="text-success">
                    {sourceWallet.currencySymbol}{parseFloat(sourceWallet.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </span>
                  <span className="text-muted"> | Remaining limit: </span>
                  <span>
                    {sourceWallet.currencySymbol}{parseFloat(sourceWallet.remainingDailyLimit).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </span>
                </div>
              )}
            </div>

            <div className="transfer-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <polyline points="19 12 12 19 5 12"></polyline>
              </svg>
            </div>

            {transferMode === 'other' ? (
              <div className="recipient-section">
                <div className="form-group">
                  <label htmlFor="recipientEmail">Recipient Email</label>
                  <div className="recipient-lookup">
                    <input
                      id="recipientEmail"
                      type="email"
                      value={recipientEmail}
                      onChange={(e) => {
                        setRecipientEmail(e.target.value);
                        setRecipientInfo(null);
                        setFormData(prev => ({ ...prev, targetWalletId: '' }));
                      }}
                      placeholder="Enter recipient's email address"
                    />
                    <button
                      type="button"
                      className="btn btn-secondary lookup-btn"
                      onClick={handleLookupRecipient}
                      disabled={lookingUp}
                    >
                      {lookingUp ? <span className="spinner" style={{ width: 16, height: 16 }}></span> : 'Find'}
                    </button>
                  </div>
                </div>

                {recipientInfo && (
                  <div className="recipient-info">
                    <div className="recipient-card">
                      <div className="recipient-avatar">
                        {recipientInfo.firstName.charAt(0).toUpperCase()}
                      </div>
                      <div className="recipient-details">
                        <span className="recipient-name">{recipientInfo.firstName}</span>
                        <span className="recipient-email">{recipientInfo.email}</span>
                      </div>
                    </div>

                    <div className="form-group">
                      <label htmlFor="targetWalletId">Send to their wallet</label>
                      <select
                        id="targetWalletId"
                        name="targetWalletId"
                        value={formData.targetWalletId}
                        onChange={handleChange}
                        required
                      >
                        <option value="">Select recipient wallet</option>
                        {recipientInfo.wallets.map(wallet => (
                          <option key={wallet.id} value={wallet.id}>
                            {wallet.currency} ({wallet.symbol})
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="form-group">
                <label htmlFor="targetWalletId">To Wallet</label>
                <select
                  id="targetWalletId"
                  name="targetWalletId"
                  value={formData.targetWalletId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select target wallet</option>
                  {wallets.map(wallet => (
                    <option key={wallet.id} value={wallet.id}>
                      {getWalletDisplay(wallet)}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="form-group">
              <label htmlFor="amount">Amount</label>
              <div className="amount-input">
                <span className="amount-symbol">
                  {sourceWallet?.currencySymbol || '$'}
                </span>
                <input
                  id="amount"
                  name="amount"
                  type="number"
                  value={formData.amount}
                  onChange={handleChange}
                  placeholder="0.00"
                  min="0.01"
                  step="0.01"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="description">Description (optional)</label>
              <input
                id="description"
                name="description"
                type="text"
                value={formData.description}
                onChange={handleChange}
                placeholder="What's this transfer for?"
              />
            </div>

            <button 
              type="submit" 
              className="btn btn-primary w-full"
              disabled={submitting || (transferMode === 'other' && !recipientInfo)}
            >
              {submitting ? (
                <>
                  <span className="spinner" style={{ width: 18, height: 18 }}></span>
                  Processing...
                </>
              ) : (
                transferMode === 'other' 
                  ? `Send to ${recipientInfo?.firstName || 'User'}` 
                  : 'Send Transfer'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default TransferPage;
