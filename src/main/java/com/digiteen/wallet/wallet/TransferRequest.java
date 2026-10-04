package com.digiteen.wallet.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID requestId,
        @NotNull UUID destinationWalletId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {}
