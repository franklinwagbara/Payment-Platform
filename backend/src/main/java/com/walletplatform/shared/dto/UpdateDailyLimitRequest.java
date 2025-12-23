package com.walletplatform.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateDailyLimitRequest {
    
    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0", message = "Daily limit cannot be negative")
    private BigDecimal dailyLimit;

    public UpdateDailyLimitRequest() {}

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
}
