package com.walletplatform.wallet.api;

import com.walletplatform.shared.dto.*;
import com.walletplatform.shared.mapper.DtoMapper;
import com.walletplatform.identity.application.service.UserService;
import com.walletplatform.wallet.application.service.WalletService;
import com.walletplatform.transaction.domain.Transaction;
import com.walletplatform.identity.domain.User;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.ledger.application.service.BalanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;
    private final DtoMapper dtoMapper;
    private final BalanceService balanceService;

    public WalletController(WalletService walletService, UserService userService, 
                           DtoMapper dtoMapper, BalanceService balanceService) {
        this.walletService = walletService;
        this.userService = userService;
        this.dtoMapper = dtoMapper;
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        List<Wallet> wallets = walletService.getUserWallets(user.getId());
        
        List<WalletResponse> response = wallets.stream()
                .map(wallet -> {
                    BigDecimal ledgerBalance = balanceService.calculateBalance(wallet.getId());
                    return dtoMapper.toWalletResponse(wallet, ledgerBalance);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet wallet = walletService.getWalletWithOwner(id);
        
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal ledgerBalance = balanceService.calculateBalance(wallet.getId());
        return ResponseEntity.ok(dtoMapper.toWalletResponse(wallet, ledgerBalance));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        String ipAddress = getClientIp(httpRequest);
        
        Wallet wallet = walletService.createWallet(user, request.getCurrency(), ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toWalletResponse(wallet));
    }

    @PostMapping("/{id}/topup")
    public ResponseEntity<TransactionResponse> topUp(
            @PathVariable UUID id,
            @Valid @RequestBody TopUpRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        Wallet wallet = walletService.getWalletWithOwner(id);
        
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String ipAddress = getClientIp(httpRequest);
        Transaction transaction = walletService.topUp(id, request.getAmount(), request.getDescription(), ipAddress);
        
        return ResponseEntity.ok(dtoMapper.toTransactionResponse(transaction));
    }

    @PatchMapping("/{id}/daily-limit")
    public ResponseEntity<WalletResponse> updateDailyLimit(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDailyLimitRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        String ipAddress = getClientIp(httpRequest);
        
        Wallet wallet = walletService.updateDailyLimit(id, request.getDailyLimit(), user.getId(), ipAddress);
        return ResponseEntity.ok(dtoMapper.toWalletResponse(wallet));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        User user = userService.getUserByEmail(userDetails.getUsername());
        // Verify wallet belongs to user
        Wallet wallet = walletService.getWalletWithOwner(id);
        if (!wallet.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String ipAddress = getClientIp(httpRequest);
        
        Transaction transaction = walletService.withdraw(
                id, 
                request.getAmount(),
                request.getBankAccountNumber(),
                request.getBankName(),
                request.getDescription(),
                ipAddress
        );
        
        return ResponseEntity.ok(dtoMapper.toTransactionResponse(transaction));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
