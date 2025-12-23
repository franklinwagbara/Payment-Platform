package com.walletplatform.shared.exception;

public class DuplicateWalletException extends RuntimeException {
    public DuplicateWalletException(String message) {
        super(message);
    }
}
