package com.digiteen.wallet.event;

import com.digiteen.wallet.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
        UUID eventId,
        UUID transactionId,
        TransactionType type,
        UUID sourceWalletId,
        UUID destinationWalletId,
        BigDecimal amount,
        String traceId,
        Instant occurredAt
) {}
