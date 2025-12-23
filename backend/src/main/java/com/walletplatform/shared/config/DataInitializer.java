package com.walletplatform.shared.config;

import com.walletplatform.identity.domain.User;
import com.walletplatform.identity.domain.UserRole;
import com.walletplatform.identity.infrastructure.UserRepository;
import com.walletplatform.wallet.domain.Currency;
import com.walletplatform.wallet.domain.Wallet;
import com.walletplatform.wallet.infrastructure.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@walletplatform.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.firstName:System}")
    private String adminFirstName;

    @Value("${app.admin.lastName:Administrator}")
    private String adminLastName;

    public DataInitializer(UserRepository userRepository, 
                          WalletRepository walletRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        log.info("Creating default admin user: {}", adminEmail);

        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .role(UserRole.ADMIN)
                .build();

        admin = userRepository.save(admin);

        // Create default USD wallet for admin
        Wallet wallet = Wallet.builder()
                .owner(admin)
                .currency(Currency.USD)
                .build();
        walletRepository.save(wallet);

        log.info("Default admin user created successfully");
        log.info("  Email: {}", adminEmail);
        log.info("  Password: {}", adminPassword);
    }
}
