package com.walletplatform.shared.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class WithdrawRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    private String description;

    public WithdrawRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private BigDecimal amount;
        private String bankAccountNumber;
        private String bankName;
        private String description;

        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder bankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; return this; }
        public Builder bankName(String bankName) { this.bankName = bankName; return this; }
        public Builder description(String description) { this.description = description; return this; }

        public WithdrawRequest build() {
            WithdrawRequest request = new WithdrawRequest();
            request.amount = this.amount;
            request.bankAccountNumber = this.bankAccountNumber;
            request.bankName = this.bankName;
            request.description = this.description;
            return request;
        }
    }
}
