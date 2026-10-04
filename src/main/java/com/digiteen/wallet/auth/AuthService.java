package com.digiteen.wallet.auth;

import com.digiteen.wallet.exception.ConflictException;
import com.digiteen.wallet.exception.UnauthorizedException;
import com.digiteen.wallet.security.JwtService;
import com.digiteen.wallet.user.UserEntity;
import com.digiteen.wallet.user.UserRepository;
import com.digiteen.wallet.wallet.Wallet;
import com.digiteen.wallet.wallet.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository users;
    private final WalletRepository wallets;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, WalletRepository wallets, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.wallets = wallets;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("email is already registered");
        Instant now = Instant.now();
        UserEntity user = new UserEntity(UUID.randomUUID(), request.name().trim(), email,
                request.phone(), passwordEncoder.encode(request.password()), now);
        users.save(user);
        Wallet wallet = wallets.save(new Wallet(UUID.randomUUID(), user, now));
        log.info("user registered userId={} walletId={}", user.getId(), wallet.getId());
        return new RegisterResponse(user.getId(), wallet.getId(), user.getEmail());
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("invalid credentials");
        }
        UUID walletId = wallets.findByUserEmail(user.getEmail()).orElseThrow().getId();
        log.info("user logged in userId={} walletId={}", user.getId(), walletId);
        return new LoginResponse(jwtService.generate(user.getEmail()), "Bearer", jwtService.expiresInSeconds(), walletId);
    }
}
