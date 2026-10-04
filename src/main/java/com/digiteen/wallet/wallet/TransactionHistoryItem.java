package com.digiteen.wallet.wallet;

import com.digiteen.wallet.transaction.TransactionStatus;
import com.digiteen.wallet.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryItem(
        UUID transactionId, UUID requestId, TransactionType type, TransactionStatus status,
        UUID sourceWalletId, UUID destinationWalletId, BigDecimal amount, BigDecimal balanceAfter,
        String traceId, String failureReason, Instant createdAt
) {}
