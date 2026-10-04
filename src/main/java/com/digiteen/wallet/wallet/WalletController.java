package com.digiteen.wallet.wallet;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final WalletApplicationService service;

    public WalletController(WalletApplicationService service) {
        this.service = service;
    }

    @PostMapping("/deposit")
    public WalletCommandResponse deposit(Authentication auth, @Valid @RequestBody MoneyRequest request) {
        return service.deposit(auth.getName(), request);
    }

    @PostMapping("/withdraw")
    public WalletCommandResponse withdraw(Authentication auth, @Valid @RequestBody MoneyRequest request) {
        return service.withdraw(auth.getName(), request);
    }

    @PostMapping("/transfer")
    public WalletCommandResponse transfer(Authentication auth, @Valid @RequestBody TransferRequest request) {
        return service.transfer(auth.getName(), request);
    }

    @GetMapping("/balance")
    public WalletBalanceResponse balance(Authentication auth) {
        return service.balance(auth.getName());
    }

    @GetMapping("/transactions")
    public List<TransactionHistoryItem> history(Authentication auth) {
        return service.history(auth.getName());
    }
}
