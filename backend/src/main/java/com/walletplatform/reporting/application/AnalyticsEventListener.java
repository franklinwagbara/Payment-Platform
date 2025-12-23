package com.walletplatform.reporting.application;

import com.walletplatform.shared.event.BalanceChangedEvent;
import com.walletplatform.shared.event.LedgerEntriesCreatedEvent;
import com.walletplatform.shared.event.TransferCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        log.info("[ANALYTICS] Transfer completed: txn={}, source={}, target={}, amount={}",
            event.getTransactionId(),
            event.getSourceWalletId(),
            event.getTargetWalletId(),
            event.getSourceAmount()
        );
        
        // TODO: Send to analytics service
        // analyticsService.trackTransfer(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBalanceChanged(BalanceChangedEvent event) {
        log.info("[ANALYTICS] Balance changed: wallet={}, {} -> {}, reason={}",
            event.getWalletId(),
            event.getPreviousBalance(),
            event.getNewBalance(),
            event.getReason()
        );
        
        // TODO: Update real-time dashboard
        // dashboardService.updateBalance(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLedgerEntriesCreated(LedgerEntriesCreatedEvent event) {
        log.info("[ANALYTICS] Ledger entries created: txn={}, count={}, type={}",
            event.getTransactionId(),
            event.getEntryCount(),
            event.getOperationType()
        );
        
        // TODO: Update audit trail
        // auditService.recordLedgerActivity(event);
    }
}
