# Digital Wallet & Payments Platform

A production-ready digital wallet system with Spring Boot backend and React frontend, featuring ledger-first architecture for bulletproof financial integrity.

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+

### Backend
```bash
cd backend
mvn spring-boot:run
```
API: `http://localhost:8080`

### Frontend
```bash
cd frontend
npm install
npm run dev
```
UI: `http://localhost:5173`

### Default Admin Account
A default admin is created on startup:
- **Email:** `admin@walletplatform.com`
- **Password:** `Admin123!`

---

## 📁 Project Structure

```
Payment Platform/
├── backend/
│   └── src/main/java/com/walletplatform/
│       ├── admin/                # Admin analytics & verification
│       │   ├── api/              # AdminController
│       │   └── application/      # AdminService
│       │
│       ├── identity/             # User management
│       │   ├── api/              # AuthController, UserController
│       │   ├── application/      # UserService
│       │   ├── domain/           # User entity
│       │   └── infrastructure/   # UserRepository
│       │
│       ├── wallet/               # Wallet operations
│       │   ├── api/              # WalletController
│       │   ├── application/      # WalletService, WalletQueryService
│       │   ├── domain/           # Wallet, Currency
│       │   └── infrastructure/   # WalletRepository
│       │
│       ├── transaction/          # Transaction processing
│       │   ├── api/              # TransactionController
│       │   ├── application/      # TransactionOrchestrator, TransactionService
│       │   ├── domain/           # Transaction, TransactionType/Status
│       │   └── infrastructure/   # TransactionRepository
│       │
│       ├── ledger/               # Double-entry bookkeeping
│       │   ├── api/              # LedgerController
│       │   ├── application/      # LedgerService, BalanceService
│       │   ├── domain/           # LedgerEntry, AccountType, EntryType
│       │   └── infrastructure/   # LedgerEntryRepository
│       │
│       ├── shared/               # Cross-cutting concerns
│       │   ├── api/              # ExchangeRateController
│       │   ├── config/           # SecurityConfig, ExchangeRateService
│       │   ├── dto/              # Request/Response DTOs
│       │   ├── event/            # Domain events
│       │   ├── exception/        # Custom exceptions
│       │   ├── infrastructure/   # IdempotencyService
│       │   ├── mapper/           # DtoMapper
│       │   └── security/         # JWT, Authentication
│       │
│       ├── reporting/            # Analytics event listeners
│       └── risk/                 # Risk assessment listeners
│
└── frontend/                     # React + Vite
    └── src/
        ├── components/           # Navbar, ProtectedRoute
        ├── contexts/             # AuthContext
        ├── pages/                # Dashboard, Transfer, History, Admin
        └── services/             # API client
```

---

## ✨ Features

### Core
- ✅ User registration & JWT authentication
- ✅ Multi-wallet support (USD, EUR, GBP)
- ✅ Top-up, transfer, withdrawal operations
- ✅ Transaction history with pagination

### Advanced
- ✅ **Double-entry ledger** - Bulletproof balance tracking
- ✅ **Ledger-first architecture** - Balance derived from entries
- ✅ **Cross-currency transfers** with live exchange rates
- ✅ **Idempotency support** - Safe retry of transfers
- ✅ **Daily spending limits** per wallet
- ✅ **Balance verification** - Admin tools for audit

### Technical
- ✅ **Modular architecture** - Domain-driven modules
- ✅ **Pessimistic locking** - Safe concurrent operations
- ✅ **Domain events** - Async notifications & analytics
- ✅ **Role-based access** - USER/ADMIN roles
- ✅ **Externalized config** - Environment-specific settings

---

## 🔌 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login & get JWT |
| GET | `/api/users/me` | Current user profile |
| GET | `/api/users/lookup` | Find user by email |

### Wallets
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallets` | List user's wallets |
| GET | `/api/wallets/{id}` | Get wallet details |
| POST | `/api/wallets` | Create wallet |
| POST | `/api/wallets/{id}/topup` | Add funds |
| POST | `/api/wallets/{id}/withdraw` | Withdraw funds |
| PATCH | `/api/wallets/{id}/daily-limit` | Update limit |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/transfer` | Transfer money |
| GET | `/api/transactions` | Transaction history |
| GET | `/api/transactions/analytics` | Spending analytics |

### Ledger
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/wallets/{id}/ledger` | Wallet ledger entries |
| GET | `/api/transactions/{id}/ledger` | Transaction entries |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/analytics` | System analytics |
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/transactions` | All transactions |
| GET | `/api/admin/balance-verification` | Verify all balances |
| GET | `/api/admin/ledger/verify` | Ledger integrity check |

### Exchange Rates
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rates` | Current exchange rates |
| GET | `/api/rates/convert` | Convert amount |

---

## 🏗️ Architecture

### Ledger-First Design
All balance operations follow a ledger-first approach:
1. **Validate** against ledger-derived balance (source of truth)
2. **Create transaction** record
3. **Write ledger entries** (double-entry)
4. **Update cached balance** (for read performance)
5. **Publish domain events** (async notifications)

### Double-Entry Bookkeeping
Every financial operation creates balanced debit/credit entries:
- **Top-up**: DEBIT System Cash, CREDIT Wallet
- **Withdrawal**: DEBIT Wallet, CREDIT System Cash
- **Transfer**: DEBIT Source, CREDIT Target
- **FX Transfer**: Uses Exchange suspense account

### Modular Architecture
Each module owns its complete vertical slice:
- **API** → Controllers, DTOs
- **Application** → Services, orchestration
- **Domain** → Entities, business rules
- **Infrastructure** → Repositories, external services

---

## 💾 Database

H2 in-memory database (accessible at `/h2-console`):
- URL: `jdbc:h2:mem:walletdb`
- User: `sa` / Password: (empty)

Tables:
- `users` - User accounts with roles
- `wallets` - Currency wallets with limits
- `transactions` - All money movements
- `ledger_entries` - Double-entry bookkeeping
- `audit_logs` - Audit trail
- `idempotency_keys` - Replay protection

---

## ⚙️ Configuration

Key settings in `application.yml`:

```yaml
# Exchange Rate API
exchange-rate:
  api:
    url: https://api.exchangerate-api.com/v4/latest/USD
    enabled: true
  refresh-interval-ms: 3600000

# JWT
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

---

## 🧪 Testing

```bash
# Backend tests
cd backend && mvn test

# Frontend dev
cd frontend && npm run dev
```

---

## 📝 Key Design Decisions

1. **Ledger is source of truth** - Cached balances can be regenerated
2. **Idempotent transfers** - Safe to retry with same key
3. **Async event processing** - Non-blocking notifications
4. **Module facades** - Clean inter-module communication
5. **Consistent lock ordering** - Prevents deadlocks
