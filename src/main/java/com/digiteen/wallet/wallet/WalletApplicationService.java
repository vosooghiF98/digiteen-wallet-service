package com.digiteen.wallet.wallet;

import com.digiteen.wallet.event.TransactionEvent;
import com.digiteen.wallet.exception.IdempotencyConflictException;
import com.digiteen.wallet.exception.InsufficientBalanceException;
import com.digiteen.wallet.exception.NotFoundException;
import com.digiteen.wallet.outbox.OutboxEvent;
import com.digiteen.wallet.outbox.OutboxEventRepository;
import com.digiteen.wallet.tracing.TraceContext;
import com.digiteen.wallet.transaction.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WalletApplicationService {
    private static final Logger log = LoggerFactory.getLogger(WalletApplicationService.class);
    private final WalletRepository wallets;
    private final WalletTransactionRepository transactions;
    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;

    public WalletApplicationService(WalletRepository wallets, WalletTransactionRepository transactions,
                                    OutboxEventRepository outbox, ObjectMapper objectMapper) {
        this.wallets = wallets;
        this.transactions = transactions;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WalletCommandResponse deposit(String email, MoneyRequest request) {
        UUID walletId = ownWalletId(email);
        Wallet wallet = lock(walletId);
        WalletCommandResponse duplicate = existingOrNull(request.requestId(), wallet.getId(),
                TransactionType.DEPOSIT, request.amount(), null);
        if (duplicate != null) return duplicate;

        wallet.credit(request.amount());
        WalletTransaction tx = saveSuccess(request.requestId(), TransactionType.DEPOSIT, wallet.getId(), null,
                request.amount(), wallet.getBalance());
        enqueue(tx);
        log.info("deposit completed transactionId={} walletId={} amount={} balanceAfter={}",
                tx.getId(), wallet.getId(), request.amount(), wallet.getBalance());
        return response(tx, false);
    }

    @Transactional(noRollbackFor = InsufficientBalanceException.class)
    public WalletCommandResponse withdraw(String email, MoneyRequest request) {
        UUID walletId = ownWalletId(email);
        Wallet wallet = lock(walletId);
        WalletCommandResponse duplicate = existingOrNull(request.requestId(), wallet.getId(),
                TransactionType.WITHDRAW, request.amount(), null);
        if (duplicate != null) return duplicate;

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            WalletTransaction failed = WalletTransaction.failed(request.requestId(), TransactionType.WITHDRAW,
                    wallet.getId(), null, request.amount(), wallet.getBalance(), TraceContext.currentTraceId(),
                    "insufficient balance", Instant.now());
            transactions.save(failed);
            log.info("withdraw rejected transactionId={} walletId={} amount={} balance={}",
                    failed.getId(), wallet.getId(), request.amount(), wallet.getBalance());
            throw new InsufficientBalanceException("insufficient balance");
        }

        wallet.debit(request.amount());
        WalletTransaction tx = saveSuccess(request.requestId(), TransactionType.WITHDRAW, wallet.getId(), null,
                request.amount(), wallet.getBalance());
        enqueue(tx);
        log.info("withdraw completed transactionId={} walletId={} amount={} balanceAfter={}",
                tx.getId(), wallet.getId(), request.amount(), wallet.getBalance());
        return response(tx, false);
    }

    @Transactional(noRollbackFor = InsufficientBalanceException.class)
    public WalletCommandResponse transfer(String email, TransferRequest request) {
        UUID sourceWalletId = ownWalletId(email);
        if (sourceWalletId.equals(request.destinationWalletId())) {
            throw new IllegalArgumentException("source and destination wallets must differ");
        }
        // lock ordering to prevent deadlock
        UUID destinationWalletId = request.destinationWalletId();

        UUID firstId;
        UUID secondId;

        if (sourceWalletId.compareTo(destinationWalletId) <= 0) {
            firstId = sourceWalletId;
            secondId = destinationWalletId;
        } else {
            firstId = destinationWalletId;
            secondId = sourceWalletId;
        }
        Wallet first = lock(firstId);
        Wallet second = lock(secondId);
        Wallet source = first.getId().equals(sourceWalletId) ? first : second;
        Wallet destination = first.getId().equals(request.destinationWalletId()) ? first : second;

        WalletCommandResponse duplicate = existingOrNull(request.requestId(), source.getId(),
                TransactionType.TRANSFER, request.amount(), destination.getId());
        if (duplicate != null) return duplicate;

        if (source.getBalance().compareTo(request.amount()) < 0) {
            WalletTransaction failed = WalletTransaction.failed(request.requestId(), TransactionType.TRANSFER,
                    source.getId(), destination.getId(), request.amount(), source.getBalance(),
                    TraceContext.currentTraceId(), "insufficient balance", Instant.now());
            transactions.save(failed);
            throw new InsufficientBalanceException("insufficient balance");
        }

        source.debit(request.amount());
        destination.credit(request.amount());
        WalletTransaction tx = saveSuccess(request.requestId(), TransactionType.TRANSFER,
                source.getId(), destination.getId(), request.amount(), source.getBalance());
        enqueue(tx);
        log.info("transfer completed transactionId={} sourceWalletId={} destinationWalletId={} amount={} sourceBalanceAfter={}",
                tx.getId(), source.getId(), destination.getId(), request.amount(), source.getBalance());
        return response(tx, false);
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse balance(String email) {
        Wallet wallet = ownWallet(email);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance());
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryItem> history(String email) {
        UUID walletId = ownWallet(email).getId();
        return transactions.findHistory(walletId);
    }

    private Wallet ownWallet(String email) {
        return wallets.findByUserEmail(email).orElseThrow(() -> new NotFoundException("wallet not found"));
    }

    private UUID ownWalletId(String email) {
        return wallets.findIdByUserEmail(email).orElseThrow(() -> new NotFoundException("wallet not found"));
    }

    private Wallet lock(UUID id) {
        return wallets.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("wallet not found: " + id));
    }

    private WalletTransaction saveSuccess(UUID requestId, TransactionType type, UUID sourceWalletId,
                                          UUID destinationWalletId, BigDecimal amount, BigDecimal balanceAfter) {
        return transactions.save(WalletTransaction.success(requestId, type, sourceWalletId, destinationWalletId,
                amount, balanceAfter, TraceContext.currentTraceId(), Instant.now()));
    }

    private WalletCommandResponse existingOrNull(UUID requestId, UUID sourceWalletId, TransactionType expectedType,
                                                 BigDecimal amount, UUID destinationWalletId) {
        WalletTransaction existing = transactions.findByRequestId(requestId).orElse(null);
        if (existing == null) return null;
        boolean same = existing.getType() == expectedType
                && sourceWalletId.equals(existing.getSourceWalletId())
                && amount.compareTo(existing.getAmount()) == 0
                && java.util.Objects.equals(destinationWalletId, existing.getDestinationWalletId());
        if (!same) throw new IdempotencyConflictException("requestId was already used with different request content");
        if (existing.getStatus() == TransactionStatus.FAILED) {
            throw new InsufficientBalanceException(existing.getFailureReason() == null ? "previous request failed" : existing.getFailureReason());
        }
        log.info("idempotent replay transactionId={} requestId={}", existing.getId(), requestId);
        return response(existing, true);
    }

    private void enqueue(WalletTransaction tx) {
        UUID eventId = UUID.randomUUID();
        TransactionEvent event = new TransactionEvent(eventId, tx.getId(), tx.getType(), tx.getSourceWalletId(),
                tx.getDestinationWalletId(), tx.getAmount(), tx.getTraceId(), Instant.now());
        try {
            String payload = objectMapper.writeValueAsString(event);
            outbox.save(new OutboxEvent(eventId, tx.getId(), "TRANSACTION_COMPLETED", payload, tx.getTraceId()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cannot serialize transaction event", e);
        }
    }

    private static WalletCommandResponse response(WalletTransaction tx, boolean duplicate) {
        return new WalletCommandResponse(tx.getId(), tx.getRequestId(), tx.getType(), tx.getStatus(),
                tx.getAmount(), tx.getBalanceAfter(), duplicate);
    }
}
