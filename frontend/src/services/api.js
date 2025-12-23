import axios from 'axios';

const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Wallet API
export const walletApi = {
  getWallets: () => api.get('/api/wallets'),
  getWallet: (id) => api.get(`/api/wallets/${id}`),
  createWallet: (currency) => api.post('/api/wallets', { currency }),
  topUp: (walletId, amount, description) => 
    api.post(`/api/wallets/${walletId}/topup`, { amount, description }),
  withdraw: (walletId, amount, bankAccountNumber, bankName, description) => 
    api.post(`/api/wallets/${walletId}/withdraw`, { 
      amount, bankAccountNumber, bankName, description 
    }),
  updateDailyLimit: (walletId, dailyLimit) =>
    api.patch(`/api/wallets/${walletId}/daily-limit`, { dailyLimit })
};

// Transaction API
export const transactionApi = {
  transfer: (sourceWalletId, targetWalletId, amount, description) =>
    api.post('/api/transactions/transfer', { sourceWalletId, targetWalletId, amount, description }),
  getTransactions: (page = 0, size = 20) =>
    api.get(`/api/transactions?page=${page}&size=${size}`),
  getWalletTransactions: (walletId, page = 0, size = 20) =>
    api.get(`/api/transactions/wallet/${walletId}?page=${page}&size=${size}`),
  getAnalytics: (walletId, days = 30) =>
    api.get(`/api/transactions/analytics?walletId=${walletId}&days=${days}`),
  getMonthlyReport: (walletId, months = 12) =>
    api.get(`/api/transactions/monthly-report?walletId=${walletId}&months=${months}`)
};

// User API
export const userApi = {
  getCurrentUser: () => api.get('/api/users/me'),
  lookupRecipient: (email) => api.get(`/api/users/lookup?email=${encodeURIComponent(email)}`)
};

// Exchange Rates API 
export const ratesApi = {
  getRates: () => api.get('/api/rates'),
  convert: (from, to, amount) => 
    api.get(`/api/rates/convert?from=${from}&to=${to}&amount=${amount}`)
};

// Admin API (requires ADMIN role)
export const adminApi = {
  getAnalytics: () => api.get('/api/admin/analytics'),
  getUsers: (page = 0, size = 20) => 
    api.get(`/api/admin/users?page=${page}&size=${size}`),
  getTransactions: (page = 0, size = 20) => 
    api.get(`/api/admin/transactions?page=${page}&size=${size}`),
  getWallets: (page = 0, size = 20) => 
    api.get(`/api/admin/wallets?page=${page}&size=${size}`),
  getUserDetails: (userId) => api.get(`/api/admin/users/${userId}`),
  // Balance verification endpoints
  getBalanceVerification: () => api.get('/api/admin/balance-verification'),
  getLedgerIntegrity: () => api.get('/api/admin/ledger-integrity'),
  verifyWalletBalance: (walletId) => api.get(`/api/admin/wallets/${walletId}/verify`),
  reconcileWallet: (walletId) => api.post(`/api/admin/wallets/${walletId}/reconcile`)
};

export default api;

