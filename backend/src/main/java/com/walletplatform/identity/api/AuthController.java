package com.walletplatform.identity.api;

import com.walletplatform.shared.dto.AuthResponse;
import com.walletplatform.shared.dto.LoginRequest;
import com.walletplatform.shared.dto.RegisterRequest;
import com.walletplatform.identity.application.service.UserService;
import com.walletplatform.identity.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                ipAddress
        );

        String token = userService.authenticate(request.getEmail(), request.getPassword(), ipAddress);

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userId(user.getId().toString())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String token = userService.authenticate(request.getEmail(), request.getPassword(), ipAddress);
        User user = userService.getUserByEmail(request.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userId(user.getId().toString())
                .role(user.getRole().name())
                .build());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
