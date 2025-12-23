package com.walletplatform.ledger.domain;

public enum AccountType {
    WALLET,       // User wallet account
    SYSTEM_CASH,  // Platform cash pool (for top-ups/withdrawals)
    EXCHANGE,     // Currency conversion suspense account
    FEE           // Transaction fee revenue account
}
