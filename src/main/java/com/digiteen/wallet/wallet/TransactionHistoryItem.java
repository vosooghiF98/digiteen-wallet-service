package com.digiteen.wallet.wallet;

import com.digiteen.wallet.transaction.TransactionStatus;
import com.digiteen.wallet.transaction.TransactionType;
import com.digiteen.wallet.transaction.WalletTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionHistoryItem {
    UUID transactionId;
    UUID requestId;
    TransactionType type;
    TransactionStatus status;
    UUID sourceWalletId;
    UUID destinationWalletId;
    BigDecimal amount;
    BigDecimal balanceAfter;
    String traceId;
    String failureReason;
    Instant createdAt;

    public TransactionHistoryItem() {
    }

    public TransactionHistoryItem(WalletTransaction t) {
        this.transactionId = t.getId();
        this.requestId = t.getRequestId();
        this.type = t.getType();
        this.status = t.getStatus();
        this.sourceWalletId = t.getSourceWalletId();
        this.destinationWalletId = t.getDestinationWalletId();
        this.amount = t.getAmount();
        this.balanceAfter = t.getBalanceAfter();
        this.traceId = t.getTraceId();
        this.failureReason = t.getFailureReason();
        this.createdAt = t.getCreatedAt();
    }

    public UUID getTransactionId() {
        return transactionId;
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
}
