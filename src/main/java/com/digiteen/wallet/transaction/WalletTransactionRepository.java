package com.digiteen.wallet.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    Optional<WalletTransaction> findByRequestId(UUID requestId);

    @Query("""
        select t from WalletTransaction t
        where t.sourceWalletId = :walletId or t.destinationWalletId = :walletId
        order by t.createdAt desc
        """)
    List<WalletTransaction> findHistory(@Param("walletId") UUID walletId);
}
