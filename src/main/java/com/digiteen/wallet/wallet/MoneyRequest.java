package com.digiteen.wallet.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record MoneyRequest(
        @NotNull UUID requestId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {}
