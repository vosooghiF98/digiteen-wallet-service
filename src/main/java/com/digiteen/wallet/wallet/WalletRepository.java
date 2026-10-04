package com.digiteen.wallet.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Query("select w from Wallet w join fetch w.user u where lower(u.email) = lower(:email)")
    Optional<Wallet> findByUserEmail(@Param("email") String email);

    @Query("select w.id from Wallet w join w.user u where lower(u.email) = lower(:email)")
    Optional<UUID> findIdByUserEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);
}
