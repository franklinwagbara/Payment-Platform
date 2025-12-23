package com.walletplatform.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    
    @NotNull(message = "Source wallet ID is required")
    private UUID sourceWalletId;
    
    @NotNull(message = "Target wallet ID is required")
    private UUID targetWalletId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;
    
    private String description;

    public TransferRequest() {}

    public UUID getSourceWalletId() { return sourceWalletId; }
    public void setSourceWalletId(UUID sourceWalletId) { this.sourceWalletId = sourceWalletId; }
    public UUID getTargetWalletId() { return targetWalletId; }
    public void setTargetWalletId(UUID targetWalletId) { this.targetWalletId = targetWalletId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
