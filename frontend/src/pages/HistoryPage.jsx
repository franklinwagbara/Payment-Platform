import { useState, useEffect } from 'react';
import { walletApi, transactionApi } from '../services/api';
import './HistoryPage.css';

function HistoryPage() {
  const [transactions, setTransactions] = useState([]);
  const [wallets, setWallets] = useState([]);
  const [selectedWallet, setSelectedWallet] = useState('all');
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    loadData();
  }, [selectedWallet]);

  const loadData = async () => {
    setLoading(true);
    try {
      const walletsResponse = await walletApi.getWallets();
      setWallets(walletsResponse.data);

      let transactionsResponse;
      if (selectedWallet === 'all') {
        transactionsResponse = await transactionApi.getTransactions(0, 20);
      } else {
        transactionsResponse = await transactionApi.getWalletTransactions(selectedWallet, 0, 20);
        const analyticsResponse = await transactionApi.getAnalytics(selectedWallet, 30);
        setAnalytics(analyticsResponse.data);
      }

      setTransactions(transactionsResponse.data.content || []);
      setHasMore(!transactionsResponse.data.last);
      setPage(0);
    } catch (err) {
      console.error('Failed to load data:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadMore = async () => {
    const nextPage = page + 1;
    try {
      let response;
      if (selectedWallet === 'all') {
        response = await transactionApi.getTransactions(nextPage, 20);
      } else {
        response = await transactionApi.getWalletTransactions(selectedWallet, nextPage, 20);
      }
      setTransactions(prev => [...prev, ...(response.data.content || [])]);
      setHasMore(!response.data.last);
      setPage(nextPage);
    } catch (err) {
      console.error('Failed to load more:', err);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getTransactionIcon = (type) => {
    switch (type) {
      case 'TOP_UP':
        return (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="12" y1="19" x2="12" y2="5"></line>
            <polyline points="5 12 12 5 19 12"></polyline>
          </svg>
        );
      case 'TRANSFER':
        return (
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polyline points="17 1 21 5 17 9"></polyline>
            <path d="M3 11V9a4 4 0 0 1 4-4h14"></path>
            <polyline points="7 23 3 19 7 15"></polyline>
            <path d="M21 13v2a4 4 0 0 1-4 4H3"></path>
          </svg>
        );
      default:
        return null;
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return <span className="badge badge-success">Completed</span>;
      case 'PENDING':
        return <span className="badge badge-warning">Pending</span>;
      case 'FAILED':
        return <span className="badge badge-error">Failed</span>;
      default:
        return <span className="badge">{status}</span>;
    }
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
    <div className="page history-page">
      <div className="container">
        <div className="history-header">
          <div>
            <h1>Transaction History</h1>
            <p className="text-muted">View and track all your transactions</p>
          </div>
          <div className="history-filter">
            <select 
              value={selectedWallet} 
              onChange={(e) => setSelectedWallet(e.target.value)}
            >
              <option value="all">All Wallets</option>
              {wallets.map(wallet => (
                <option key={wallet.id} value={wallet.id}>
                  {wallet.currency} Wallet
                </option>
              ))}
            </select>
          </div>
        </div>

        {analytics && selectedWallet !== 'all' && (
          <div className="analytics-card animate-fade-in">
            <h3>30-Day Summary</h3>
            <div className="analytics-stats">
              <div className="stat">
                <span className="stat-value">
                  ${parseFloat(analytics.totalTransferred || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
                <span className="stat-label text-muted">Total Transferred</span>
              </div>
              <div className="stat">
                <span className="stat-value text-success">{analytics.completedTransactions || 0}</span>
                <span className="stat-label text-muted">Completed</span>
              </div>
              <div className="stat">
                <span className="stat-value text-error">{analytics.failedTransactions || 0}</span>
                <span className="stat-label text-muted">Failed</span>
              </div>
            </div>
          </div>
        )}

        <div className="transactions-list">
          {transactions.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <h3>No Transactions Yet</h3>
              <p className="text-muted">Your transaction history will appear here</p>
            </div>
          ) : (
            <>
              {transactions.map((transaction) => (
                <div key={transaction.id} className="transaction-item animate-fade-in">
                  <div className={`transaction-icon ${transaction.type.toLowerCase()}`}>
                    {getTransactionIcon(transaction.type)}
                  </div>
                  <div className="transaction-details">
                    <div className="transaction-title">
                      {transaction.type === 'TOP_UP' ? 'Top Up' : 'Transfer'}
                      {transaction.description && ` - ${transaction.description}`}
                    </div>
                    <div className="transaction-meta text-muted">
                      {formatDate(transaction.createdAt)}
                      {transaction.targetCurrency && transaction.sourceCurrency !== transaction.targetCurrency && (
                        <span className="exchange-rate">
                          {' '}• Rate: 1 {transaction.sourceCurrency} = {parseFloat(transaction.exchangeRate).toFixed(4)} {transaction.targetCurrency}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="transaction-amount">
                    {(() => {
                      // Determine if this is incoming or outgoing for transfers
                      const userWalletIds = wallets.map(w => w.id);
                      const isIncoming = transaction.type === 'TOP_UP' || 
                                        (transaction.type === 'TRANSFER' && 
                                         transaction.targetWalletId && 
                                         userWalletIds.includes(transaction.targetWalletId) &&
                                         !userWalletIds.includes(transaction.sourceWalletId));
                      const isOutgoing = transaction.type === 'WITHDRAWAL' ||
                                        (transaction.type === 'TRANSFER' && 
                                         transaction.sourceWalletId && 
                                         userWalletIds.includes(transaction.sourceWalletId));
                      
                      // For transfers between own wallets, show as outgoing (source perspective)
                      const sign = isIncoming && !isOutgoing ? '+' : '-';
                      const colorClass = isIncoming && !isOutgoing ? 'incoming' : 'outgoing';
                      
                      return (
                        <div className={`amount ${colorClass}`}>
                          {sign}${parseFloat(transaction.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </div>
                      );
                    })()}
                    {getStatusBadge(transaction.status)}
                  </div>
                </div>
              ))}

              {hasMore && (
                <button onClick={loadMore} className="btn btn-secondary w-full mt-lg">
                  Load More
                </button>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default HistoryPage;
