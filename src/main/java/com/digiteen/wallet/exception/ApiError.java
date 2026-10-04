package com.digiteen.wallet.exception;
import java.time.Instant;
public record ApiError(Instant timestamp, int status, String code, String message, String traceId) {}
