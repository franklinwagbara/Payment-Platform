package com.walletplatform.transaction.domain;

public enum TransactionType {
    TRANSFER,   // Transfer between wallets
    TOP_UP,     // Adding funds to wallet
    WITHDRAWAL  // Removing funds from wallet (future use)
}
