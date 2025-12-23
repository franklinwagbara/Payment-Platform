package com.walletplatform.transaction.domain;

public enum TransactionStatus {
    PENDING,    // Transaction initiated but not completed
    COMPLETED,  // Transaction successfully completed
    FAILED,     // Transaction failed
    CANCELLED   // Transaction cancelled by user
}
