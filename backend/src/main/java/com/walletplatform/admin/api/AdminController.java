package com.walletplatform.admin.api;

import com.walletplatform.admin.application.service.AdminService;
import com.walletplatform.shared.dto.TransactionResponse;
import com.walletplatform.shared.dto.UserResponse;
import com.walletplatform.shared.dto.WalletResponse;
import com.walletplatform.shared.mapper.DtoMapper;
import com.walletplatform.wallet.domain.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DtoMapper dtoMapper;

    public AdminController(AdminService adminService, DtoMapper dtoMapper) {
        this.adminService = adminService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> response = adminService.getAllUsers(pageable)
                .map(dtoMapper::toUserResponse);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable UUID userId) {
        return adminService.getUserById(userId)
                .map(user -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("user", dtoMapper.toUserResponse(user));
                    
                    var wallets = adminService.getUserWallets(userId);
                    details.put("wallets", wallets.stream()
                            .map(dtoMapper::toWalletResponse)
                            .collect(Collectors.toList()));
                    
                    details.put("walletCount", wallets.size());
                    details.put("totalBalance", wallets.stream()
                            .map(Wallet::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    
                    return ResponseEntity.ok(details);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam String role) {
        
        try {
            return adminService.updateUserRole(userId, role)
                    .map(user -> ResponseEntity.ok(dtoMapper.toUserResponse(user)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role: " + role);
        }
    }

    @GetMapping("/wallets")
    public ResponseEntity<Page<WalletResponse>> getAllWallets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WalletResponse> response = adminService.getAllWallets(pageable)
                .map(dtoMapper::toWalletResponse);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionResponse> response = adminService.getAllTransactions(pageable)
                .map(dtoMapper::toTransactionResponse);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getSystemAnalytics() {
        return ResponseEntity.ok(adminService.getSystemAnalytics());
    }

    @GetMapping("/balance-verification")
    public ResponseEntity<Map<String, Object>> verifyBalances() {
        return ResponseEntity.ok(adminService.verifyAllBalances());
    }

    @GetMapping("/ledger-integrity")
    public ResponseEntity<Map<String, Object>> verifyLedgerIntegrity() {
        return ResponseEntity.ok(adminService.verifyLedgerIntegrity());
    }

    @GetMapping("/wallets/{walletId}/verify")
    public ResponseEntity<Map<String, Object>> verifyWalletBalance(@PathVariable UUID walletId) {
        return adminService.verifyWalletBalance(walletId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/wallets/{walletId}/reconcile")
    public ResponseEntity<Map<String, Object>> reconcileWalletBalance(@PathVariable UUID walletId) {
        return adminService.reconcileWalletBalance(walletId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
