package com.walletplatform.shared.application;

import com.walletplatform.shared.event.TopUpCompletedEvent;
import com.walletplatform.shared.event.TransferCompletedEvent;
import com.walletplatform.shared.event.WithdrawalCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for sending notifications (email, push, SMS).
 */
@Service
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    /**
     * Send notification when transfer is completed.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        log.info("[NOTIFICATION] Transfer notification: {} {} from wallet {} to wallet {}",
            event.getSourceAmount(),
            event.getSourceCurrency(),
            event.getSourceWalletId(),
            event.getTargetWalletId()
        );
        
        // TODO: Send notification to sender
        // notificationService.sendEmail(
        //     getUserEmail(event.getSourceWalletId()),
        //     "Transfer Sent",
        //     "You sent " + event.getSourceAmount() + " " + event.getSourceCurrency()
        // );
        
        // TODO: Send notification to recipient
        // notificationService.sendEmail(
        //     getUserEmail(event.getTargetWalletId()),
        //     "Money Received!",
        //     "You received " + event.getTargetAmount() + " " + event.getTargetCurrency()
        // );
    }

    /**
     * Send notification when top-up is completed.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTopUpCompleted(TopUpCompletedEvent event) {
        log.info("[NOTIFICATION] Top-up notification: {} {} to wallet {}",
            event.getAmount(),
            event.getCurrency(),
            event.getWalletId()
        );
        
        // TODO: Send confirmation email
        // notificationService.sendEmail(
        //     getUserEmail(event.getUserId()),
        //     "Wallet Funded",
        //     "Your wallet has been funded with " + event.getAmount() + " " + event.getCurrency()
        // );
    }

    /**
     * Send notification when withdrawal is completed.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWithdrawalCompleted(WithdrawalCompletedEvent event) {
        log.info("[NOTIFICATION] Withdrawal notification: {} {} from wallet {} to {}",
            event.getAmount(),
            event.getCurrency(),
            event.getWalletId(),
            event.getBankName()
        );
        
        // TODO: Send confirmation email with bank details
        // notificationService.sendEmail(
        //     getUserEmail(event.getUserId()),
        //     "Withdrawal Processed",
        //     "Your withdrawal of " + event.getAmount() + " " + event.getCurrency() + 
        //     " has been sent to " + event.getBankName()
        // );
    }
}
