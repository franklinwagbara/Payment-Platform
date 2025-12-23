package com.walletplatform.identity.api;

import com.walletplatform.shared.dto.UserResponse;
import com.walletplatform.shared.mapper.DtoMapper;
import com.walletplatform.identity.application.service.UserService;
import com.walletplatform.wallet.application.service.WalletService;
import com.walletplatform.identity.domain.User;
import com.walletplatform.wallet.domain.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final WalletService walletService;
    private final DtoMapper dtoMapper;

    public UserController(UserService userService, WalletService walletService, DtoMapper dtoMapper) {
        this.userService = userService;
        this.walletService = walletService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(dtoMapper.toUserResponse(user));
    }

    @GetMapping("/lookup")
    public ResponseEntity<RecipientInfo> lookupRecipient(
            @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (email.equalsIgnoreCase(userDetails.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            User recipient = userService.getUserByEmail(email);
            List<Wallet> wallets = walletService.getUserWallets(recipient.getId());
            
            List<WalletInfo> walletInfos = wallets.stream()
                    .filter(Wallet::isActive)
                    .map(w -> new WalletInfo(w.getId().toString(), w.getCurrency().name(), w.getCurrency().getSymbol()))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new RecipientInfo(
                    recipient.getFirstName(),
                    recipient.getEmail(),
                    walletInfos
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    public record WalletInfo(String id, String currency, String symbol) {}
    public record RecipientInfo(String firstName, String email, List<WalletInfo> wallets) {}
}
