package com.digiteen.wallet.auth;
import java.util.UUID;
public record RegisterResponse(UUID userId, UUID walletId, String email) {}
