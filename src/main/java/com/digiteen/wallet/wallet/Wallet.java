package com.digiteen.wallet.wallet;

import com.digiteen.wallet.user.UserEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(UUID id, UserEntity user, Instant now) {
        this.id = id;
        this.user = user;
        this.balance = BigDecimal.ZERO.setScale(2);
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        this.balance = this.balance.add(amount);
        this.updatedAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
