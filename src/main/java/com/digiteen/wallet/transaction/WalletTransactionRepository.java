package com.digiteen.wallet.transaction;

import com.digiteen.wallet.wallet.TransactionHistoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    Optional<WalletTransaction> findByRequestId(UUID requestId);

    @Query("""
        select new com.digiteen.wallet.wallet.TransactionHistoryItem(t) from WalletTransaction t
        where t.sourceWalletId = :walletId or t.destinationWalletId = :walletId
        order by t.createdAt desc
        """)
    List<TransactionHistoryItem> findHistory(@Param("walletId") UUID walletId);
}
