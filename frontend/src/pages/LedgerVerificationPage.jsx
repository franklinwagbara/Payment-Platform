import { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import './LedgerVerificationPage.css';

const LedgerVerificationPage = () => {
  const [balanceVerification, setBalanceVerification] = useState(null);
  const [ledgerIntegrity, setLedgerIntegrity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reconciling, setReconciling] = useState(null);

  useEffect(() => {
    fetchVerificationData();
  }, []);

  const fetchVerificationData = async () => {
    try {
      setLoading(true);
      setError('');
      const [balanceRes, ledgerRes] = await Promise.all([
        adminApi.getBalanceVerification(),
        adminApi.getLedgerIntegrity()
      ]);
      setBalanceVerification(balanceRes.data);
      setLedgerIntegrity(ledgerRes.data);
    } catch (err) {
      setError('Failed to load verification data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleReconcile = async (walletId) => {
    try {
      setReconciling(walletId);
      await adminApi.reconcileWallet(walletId);
      await fetchVerificationData();
    } catch (err) {
      setError('Failed to reconcile wallet');
    } finally {
      setReconciling(null);
    }
  };

  if (loading) {
    return <div className="verification-page"><div className="loading">Loading verification data...</div></div>;
  }

  return (
    <div className="verification-page">
      <h1>Ledger Verification</h1>
      
      {error && <div className="error-banner">{error}</div>}

      <section className="verification-section">
        <h2>Ledger Integrity</h2>
        <div className={`integrity-status ${ledgerIntegrity?.allBalanced ? 'balanced' : 'unbalanced'}`}>
          {ledgerIntegrity?.allBalanced ? '✓ All Balanced' : '⚠ Imbalance Detected'}
        </div>
        <div className="integrity-details">
          <span>Total Entries: {ledgerIntegrity?.entryCount || 0}</span>
        </div>
        <div className="currency-grid">
          {ledgerIntegrity && Object.entries(ledgerIntegrity)
            .filter(([key]) => !['allBalanced', 'entryCount'].includes(key))
            .map(([currency, data]) => (
              <div key={currency} className={`currency-card ${data.balanced ? 'balanced' : 'unbalanced'}`}>
                <h4>{currency}</h4>
                <div className="currency-stats">
                  <span>Debits: {parseFloat(data.totalDebits).toFixed(2)}</span>
                  <span>Credits: {parseFloat(data.totalCredits).toFixed(2)}</span>
                </div>
                <div className={`status-badge ${data.balanced ? 'success' : 'error'}`}>
                  {data.balanced ? 'Balanced' : 'Mismatch'}
                </div>
              </div>
            ))}
        </div>
      </section>

      <section className="verification-section">
        <h2>Wallet Balance Verification</h2>
        <div className="summary-stats">
          <div className="stat-card">
            <span className="stat-value">{balanceVerification?.totalWallets || 0}</span>
            <span className="stat-label">Total Wallets</span>
          </div>
          <div className="stat-card success">
            <span className="stat-value">{balanceVerification?.consistentCount || 0}</span>
            <span className="stat-label">Consistent</span>
          </div>
          <div className="stat-card error">
            <span className="stat-value">{balanceVerification?.discrepancyCount || 0}</span>
            <span className="stat-label">Discrepancies</span>
          </div>
        </div>

        {balanceVerification?.discrepancies?.length > 0 && (
          <div className="discrepancies-table">
            <h3>Discrepancies Found</h3>
            <table>
              <thead>
                <tr>
                  <th>Wallet ID</th>
                  <th>Currency</th>
                  <th>Cached</th>
                  <th>Ledger</th>
                  <th>Difference</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {balanceVerification.discrepancies.map(d => (
                  <tr key={d.walletId}>
                    <td className="wallet-id">{d.walletId.substring(0, 8)}...</td>
                    <td>{d.currency}</td>
                    <td>{parseFloat(d.cachedBalance).toFixed(2)}</td>
                    <td>{parseFloat(d.ledgerBalance).toFixed(2)}</td>
                    <td className="discrepancy">{parseFloat(d.discrepancy).toFixed(2)}</td>
                    <td>
                      <button 
                        className="reconcile-btn"
                        onClick={() => handleReconcile(d.walletId)}
                        disabled={reconciling === d.walletId}
                      >
                        {reconciling === d.walletId ? 'Reconciling...' : 'Reconcile'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {balanceVerification?.allConsistent && (
          <div className="all-consistent-banner">
            ✓ All wallet balances are consistent with ledger
          </div>
        )}
      </section>

      <button className="refresh-btn" onClick={fetchVerificationData}>
        ↻ Refresh Verification
      </button>
    </div>
  );
};

export default LedgerVerificationPage;
