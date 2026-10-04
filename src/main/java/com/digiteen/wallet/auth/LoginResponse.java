package com.digiteen.wallet.auth;
import java.util.UUID;
public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, UUID walletId) {}
