package com.matcher.platform.security.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe Circuit Breaker for an individual Mail Provider.
 * Prevents stalling requests on known-failing or IP-blocked email services.
 */
public class ProviderCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(ProviderCircuitBreaker.class);

    private final String providerName;
    private final int failureThreshold;
    private final long cooldownDurationMs;

    private volatile CircuitState state = CircuitState.CLOSED;
    private volatile long lastTrippedTimestamp = 0L;
    private volatile String lastErrorMessage = null;
    private volatile Instant lastSuccessTime = null;
    private volatile Instant lastFailureTime = null;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong lastLatencyMs = new AtomicLong(0);

    public ProviderCircuitBreaker(String providerName) {
        this(providerName, 2, 5 * 60 * 1000L); // Default: 2 failures, 5 min cooldown
    }

    public ProviderCircuitBreaker(String providerName, int failureThreshold, long cooldownDurationMs) {
        this.providerName = providerName;
        this.failureThreshold = failureThreshold;
        this.cooldownDurationMs = cooldownDurationMs;
    }

    /**
     * Determines whether a request should be dispatched to this provider.
     */
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();

        if (state == CircuitState.CLOSED) {
            return true;
        }

        if (state == CircuitState.OPEN) {
            if (now - lastTrippedTimestamp >= cooldownDurationMs) {
                state = CircuitState.HALF_OPEN;
                log.info("[CIRCUIT BREAKER] Provider '{}' transitioned from OPEN to HALF_OPEN (probing recovery).", providerName);
                return true;
            }
            return false;
        }

        // HALF_OPEN: allow trial request
        return true;
    }

    /**
     * Records a successful dispatch. Resets failure counters and restores CLOSED state.
     */
    public synchronized void recordSuccess(long latencyMs) {
        totalRequests.incrementAndGet();
        successCount.incrementAndGet();
        consecutiveFailures.set(0);
        lastLatencyMs.set(latencyMs);
        lastSuccessTime = Instant.now();

        if (state != CircuitState.CLOSED) {
            log.info("[CIRCUIT BREAKER] Provider '{}' recovered! State restored to CLOSED. Latency: {}ms", providerName, latencyMs);
            state = CircuitState.CLOSED;
            lastErrorMessage = null;
        }
    }

    /**
     * Records a failed dispatch. If fatal (e.g., HTTP 401 IP block), trips immediately.
     */
    public synchronized void recordFailure(String reason, boolean isFatal) {
        totalRequests.incrementAndGet();
        failureCount.incrementAndGet();
        lastFailureTime = Instant.now();
        lastErrorMessage = reason;

        int failures = consecutiveFailures.incrementAndGet();

        if (isFatal || state == CircuitState.HALF_OPEN || failures >= failureThreshold) {
            state = CircuitState.OPEN;
            lastTrippedTimestamp = System.currentTimeMillis();
            log.warn("[CIRCUIT BREAKER] Provider '{}' TRIPPED to OPEN! Reason: {}. (Cooldown: {}s)",
                    providerName, reason, cooldownDurationMs / 1000);
        } else {
            log.warn("[CIRCUIT BREAKER] Provider '{}' recorded failure ({}/{}): {}",
                    providerName, failures, failureThreshold, reason);
        }
    }

    public String getProviderName() {
        return providerName;
    }

    public CircuitState getState() {
        return state;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public Instant getLastSuccessTime() {
        return lastSuccessTime;
    }

    public Instant getLastFailureTime() {
        return lastFailureTime;
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getSuccessCount() {
        return successCount.get();
    }

    public long getFailureCount() {
        return failureCount.get();
    }

    public long getLastLatencyMs() {
        return lastLatencyMs.get();
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }
}
