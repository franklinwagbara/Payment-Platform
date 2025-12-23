package com.walletplatform.ledger.domain;

/**
 * Entry types for double-entry bookkeeping.
 * Every transaction creates balanced DEBIT and CREDIT entries.
 */
public enum EntryType {
    DEBIT,  // Money leaving an account (asset increase or liability decrease)
    CREDIT  // Money entering an account (asset decrease or liability increase)
}
