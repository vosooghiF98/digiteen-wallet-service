package com.digiteen.wallet.transaction;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {
    @Id
    private UUID id;
    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;
    @Column(name = "source_wallet_id")
    private UUID sourceWalletId;
    @Column(name = "destination_wallet_id")
    private UUID destinationWalletId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    @Column(name = "trace_id", nullable = false, length = 100)
    private String traceId;
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    protected WalletTransaction() {
    }

    public static WalletTransaction success(UUID requestId, TransactionType type, UUID sourceWalletId,
                                            UUID destinationWalletId, BigDecimal amount, BigDecimal balanceAfter,
                                            String traceId, Instant now) {
        WalletTransaction tx = new WalletTransaction();
        tx.id = UUID.randomUUID();
        tx.requestId = requestId;
        tx.type = type;
        tx.status = TransactionStatus.SUCCESS;
        tx.sourceWalletId = sourceWalletId;
        tx.destinationWalletId = destinationWalletId;
        tx.amount = amount;
        tx.balanceAfter = balanceAfter;
        tx.traceId = traceId;
        tx.createdAt = now;
        tx.completedAt = now;
        return tx;
    }

    public static WalletTransaction failed(UUID requestId, TransactionType type, UUID sourceWalletId,
                                           UUID destinationWalletId, BigDecimal amount, BigDecimal balanceAfter,
                                           String traceId, String reason, Instant now) {
        WalletTransaction tx = success(requestId, type, sourceWalletId, destinationWalletId, amount, balanceAfter, traceId, now);
        tx.status = TransactionStatus.FAILED;
        tx.failureReason = reason;
        return tx;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public UUID getSourceWalletId() {
        return sourceWalletId;
    }

    public UUID getDestinationWalletId() {
        return destinationWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
