package com.walletplatform.identity.domain;

import com.walletplatform.wallet.domain.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wallet> wallets = new ArrayList<>();

    public User() {}

    public User(UUID id, String email, String passwordHash, String firstName, String lastName, 
                UserRole role, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Wallet> getWallets() { return wallets; }

    public void setId(UUID id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(UserRole role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setWallets(List<Wallet> wallets) { this.wallets = wallets; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        wallet.setOwner(this);
    }

    public void removeWallet(Wallet wallet) {
        wallets.remove(wallet);
        wallet.setOwner(null);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private UserRole role = UserRole.USER;
        private boolean active = true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.email = this.email;
            user.passwordHash = this.passwordHash;
            user.firstName = this.firstName;
            user.lastName = this.lastName;
            user.role = this.role;
            user.active = this.active;
            return user;
        }
    }
}
