package com.walletplatform.ledger.api;

import com.walletplatform.ledger.application.service.LedgerService;
import com.walletplatform.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/wallets/{walletId}/ledger")
    public ResponseEntity<Page<Map<String, Object>>> getWalletLedger(
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntry> entries = ledgerService.getWalletLedger(walletId, pageable);
        
        Page<Map<String, Object>> response = entries.map(this::mapLedgerEntry);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{transactionId}/ledger")
    public ResponseEntity<List<Map<String, Object>>> getTransactionLedger(
            @PathVariable UUID transactionId) {
        
        List<LedgerEntry> entries = ledgerService.getTransactionEntries(transactionId);
        List<Map<String, Object>> response = entries.stream()
                .map(this::mapLedgerEntry)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/ledger/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> verifyLedgerBalances() {
        Map<String, Object> verification = ledgerService.verifyAllBalances();
        return ResponseEntity.ok(verification);
    }

    @GetMapping("/admin/wallets/{walletId}/ledger-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getWalletLedgerBalance(@PathVariable UUID walletId) {
        BigDecimal ledgerBalance = ledgerService.calculateWalletBalanceFromLedger(walletId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("walletId", walletId);
        response.put("ledgerBalance", ledgerBalance);
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapLedgerEntry(LedgerEntry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getId());
        map.put("transactionId", entry.getTransaction().getId());
        map.put("accountType", entry.getAccountType().name());
        map.put("entryType", entry.getEntryType().name());
        map.put("amount", entry.getAmount());
        map.put("currency", entry.getCurrency().name());
        map.put("description", entry.getDescription());
        map.put("createdAt", entry.getCreatedAt());
        
        if (entry.getWallet() != null) {
            map.put("walletId", entry.getWallet().getId());
        }
        
        return map;
    }
}
