package com.digiteen.wallet.tracing;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceContext {
    public static final String TRACE_ID = "traceId";

    private TraceContext() {
    }

    public static String currentTraceId() {
        String traceId = MDC.get(TRACE_ID);
        return traceId != null && !traceId.isBlank() ? traceId : UUID.randomUUID().toString();
    }
}
