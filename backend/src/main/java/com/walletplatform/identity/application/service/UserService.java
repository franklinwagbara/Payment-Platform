package com.walletplatform.identity.application.service;

import com.walletplatform.shared.event.AuditEvent;
import com.walletplatform.shared.exception.UserNotFoundException;
import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.identity.domain.User;
import com.walletplatform.identity.infrastructure.UserRepository;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import com.walletplatform.shared.security.JwtTokenProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User registerUser(String email, String password, String firstName, String lastName, String ipAddress) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .build();

        user = userRepository.save(user);

        // Create a default USD wallet for the new user
        Wallet defaultWallet = Wallet.builder()
                .owner(user)
                .currency(Currency.USD)
                .build();
        walletRepository.save(defaultWallet);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                user.getId(),
                "USER_REGISTERED",
                "User",
                user.getId(),
                Map.of("email", email),
                ipAddress
        ));

        return user;
    }

    @Transactional(readOnly = true)
    public String authenticate(String email, String password, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        eventPublisher.publishEvent(new AuditEvent(
                this,
                user.getId(),
                "USER_LOGIN",
                "User",
                user.getId(),
                Map.of("email", email),
                ipAddress
        ));

        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
