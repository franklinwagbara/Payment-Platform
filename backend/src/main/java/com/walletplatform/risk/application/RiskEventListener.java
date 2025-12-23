package com.walletplatform.risk.application;

import com.walletplatform.shared.event.TransferCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

/**
 * Event listener for risk assessment.
 * Monitors transfers for suspicious patterns.
 */
@Service
public class RiskEventListener {

    private static final Logger log = LoggerFactory.getLogger(RiskEventListener.class);
    
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("5000.00");

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        // Check for high-value transfer
        if (event.getSourceAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            log.warn("[RISK] High-value transfer detected: txn={}, amount={} {}",
                event.getTransactionId(),
                event.getSourceAmount(),
                event.getSourceCurrency()
            );
            
            // TODO: Create risk alert
            // riskAlertService.createHighValueAlert(event);
        }

        // Check for cross-currency transfer (higher risk)
        if (!event.getSourceCurrency().equals(event.getTargetCurrency())) {
            log.info("[RISK] Cross-currency transfer: {} {} -> {} {}",
                event.getSourceAmount(),
                event.getSourceCurrency(),
                event.getTargetAmount(),
                event.getTargetCurrency()
            );
            
            // TODO: Additional FX compliance checks
            // complianceService.checkFxTransfer(event);
        }
    }
}
