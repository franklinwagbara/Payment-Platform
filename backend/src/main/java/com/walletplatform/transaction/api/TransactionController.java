package com.walletplatform.transaction.api;

import com.walletplatform.shared.dto.TransactionResponse;
import com.walletplatform.shared.dto.TransferRequest;
import com.walletplatform.shared.mapper.DtoMapper;
import com.walletplatform.transaction.application.service.TransactionService;
import com.walletplatform.identity.application.service.UserService;
import com.walletplatform.wallet.application.service.WalletService;
import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.identity.domain.User;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.shared.infrastructure.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;
    private final UserService userService;
    private final DtoMapper dtoMapper;
    private final IdempotencyService idempotencyService;

    public TransactionController(TransactionService transactionService, WalletService walletService,
                                  UserService userService, DtoMapper dtoMapper, 
                                  IdempotencyService idempotencyService) {
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.userService = userService;
        this.dtoMapper = dtoMapper;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet sourceWallet = walletService.getWalletWithOwner(request.getSourceWalletId());
        
        if (!sourceWallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String ipAddress = getClientIp(httpRequest);
        
        // Execute with idempotency if key provided
        TransactionResponse response;
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            response = idempotencyService.executeIdempotent(
                idempotencyKey,
                TransactionResponse.class,
                () -> {
                    Transaction txn = walletService.transfer(
                        request.getSourceWalletId(),
                        request.getTargetWalletId(),
                        request.getAmount(),
                        request.getDescription(),
                        ipAddress
                    );
                    return dtoMapper.toTransactionResponse(txn);
                }
            );
        } else {
            Transaction transaction = walletService.transfer(
                request.getSourceWalletId(),
                request.getTargetWalletId(),
                request.getAmount(),
                request.getDescription(),
                ipAddress
            );
            response = dtoMapper.toTransactionResponse(transaction);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Transaction> transactions = transactionService.getTransactionsByUser(user.getId(), pageable);
        Page<TransactionResponse> response = transactions.map(dtoMapper::toTransactionResponse);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Page<TransactionResponse>> getWalletTransactions(
            @PathVariable UUID walletId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet wallet = walletService.getWalletWithOwner(walletId);
        
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getTransactionsByWallet(walletId, pageable);
        Page<TransactionResponse> response = transactions.map(dtoMapper::toTransactionResponse);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam UUID walletId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet wallet = walletService.getWalletWithOwner(walletId);
        
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        Map<String, Object> analytics = transactionService.getWalletAnalytics(walletId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/monthly-report")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyReport(
            @RequestParam UUID walletId,
            @RequestParam(defaultValue = "12") int months,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet wallet = walletService.getWalletWithOwner(walletId);
        
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Map<String, Object>> report = transactionService.getMonthlySpendingReport(walletId, months);
        return ResponseEntity.ok(report);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
