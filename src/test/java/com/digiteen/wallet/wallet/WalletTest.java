package com.digiteen.wallet.wallet;

import com.digiteen.wallet.user.UserEntity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {
    private Wallet wallet() {
        UserEntity user = new UserEntity(UUID.randomUUID(), "Test", "test@example.com", null, "hash", Instant.now());
        return new Wallet(UUID.randomUUID(), user, Instant.now());
    }

    @Test
    void creditAndDebitPreserveBalance() {
        Wallet w = wallet();
        w.credit(new BigDecimal("100.00"));
        w.debit(new BigDecimal("40.00"));
        assertEquals(0, w.getBalance().compareTo(new BigDecimal("60.00")));
    }

    @Test
    void debitCannotMakeBalanceNegative() {
        Wallet w = wallet();
        w.credit(new BigDecimal("10.00"));
        assertThrows(IllegalStateException.class, () -> w.debit(new BigDecimal("11.00")));
        assertEquals(0, w.getBalance().compareTo(new BigDecimal("10.00")));
    }
}
