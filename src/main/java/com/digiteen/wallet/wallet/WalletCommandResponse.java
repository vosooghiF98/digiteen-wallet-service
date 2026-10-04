package com.digiteen.wallet.wallet;

import com.digiteen.wallet.transaction.TransactionStatus;
import com.digiteen.wallet.transaction.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;

public record WalletCommandResponse(
        UUID transactionId,
        UUID requestId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceAfter,
        boolean duplicate
) {}
