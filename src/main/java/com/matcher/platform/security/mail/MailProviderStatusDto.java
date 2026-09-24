package com.matcher.platform.security.mail;

import java.time.Instant;

public record MailProviderStatusDto(
        String providerName,
        boolean configured,
        int priority,
        CircuitState circuitState,
        long totalRequests,
        long successCount,
        long failureCount,
        long lastLatencyMs,
        String lastErrorMessage,
        Instant lastSuccessTime,
        Instant lastFailureTime
) {}
