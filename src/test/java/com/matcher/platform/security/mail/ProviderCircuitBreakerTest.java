package com.matcher.platform.security.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderCircuitBreakerTest {

    @Test
    @DisplayName("Circuit Breaker should start CLOSED and allow requests")
    void testInitialStateClosed() {
        ProviderCircuitBreaker cb = new ProviderCircuitBreaker("TestVendor", 2, 1000L);

        assertThat(cb.getState()).isEqualTo(CircuitState.CLOSED);
        assertThat(cb.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("Circuit Breaker should trip to OPEN when fatal error (e.g. Brevo 401 IP restriction) occurs")
    void testTripOnFatalError() {
        ProviderCircuitBreaker cb = new ProviderCircuitBreaker("Brevo", 2, 5000L);

        cb.recordFailure("401 Unauthorized: IP not recognized", true);

        assertThat(cb.getState()).isEqualTo(CircuitState.OPEN);
        assertThat(cb.allowRequest()).isFalse();
        assertThat(cb.getFailureCount()).isEqualTo(1L);
        assertThat(cb.getLastErrorMessage()).contains("401 Unauthorized");
    }

    @Test
    @DisplayName("Circuit Breaker should trip to OPEN after threshold consecutive non-fatal failures")
    void testTripAfterThresholdFailures() {
        ProviderCircuitBreaker cb = new ProviderCircuitBreaker("Resend", 2, 5000L);

        cb.recordFailure("500 Server Error", false);
        assertThat(cb.getState()).isEqualTo(CircuitState.CLOSED);
        assertThat(cb.allowRequest()).isTrue();

        cb.recordFailure("503 Service Unavailable", false);
        assertThat(cb.getState()).isEqualTo(CircuitState.OPEN);
        assertThat(cb.allowRequest()).isFalse();
    }

    @Test
    @DisplayName("Circuit Breaker should recover to CLOSED after successful request in HALF_OPEN")
    void testRecoveryToClosed() throws InterruptedException {
        // Cooldown of 50ms for testing
        ProviderCircuitBreaker cb = new ProviderCircuitBreaker("SendGrid", 1, 50L);

        cb.recordFailure("Timeout", false);
        assertThat(cb.getState()).isEqualTo(CircuitState.OPEN);
        assertThat(cb.allowRequest()).isFalse();

        // Wait for cooldown
        Thread.sleep(60L);

        // First call after cooldown transitions to HALF_OPEN
        assertThat(cb.allowRequest()).isTrue();
        assertThat(cb.getState()).isEqualTo(CircuitState.HALF_OPEN);

        // Successful trial restores CLOSED
        cb.recordSuccess(120L);
        assertThat(cb.getState()).isEqualTo(CircuitState.CLOSED);
        assertThat(cb.getSuccessCount()).isEqualTo(1L);
        assertThat(cb.getLastLatencyMs()).isEqualTo(120L);
    }

    @Test
    @DisplayName("Circuit Breaker should immediately trip back to OPEN if trial request fails during HALF_OPEN")
    void testFailureInHalfOpenTripsToOpen() throws InterruptedException {
        ProviderCircuitBreaker cb = new ProviderCircuitBreaker("Mailgun", 3, 50L);

        // Force to OPEN
        cb.recordFailure("Fatal outage", true);
        assertThat(cb.getState()).isEqualTo(CircuitState.OPEN);

        Thread.sleep(60L);
        assertThat(cb.allowRequest()).isTrue();
        assertThat(cb.getState()).isEqualTo(CircuitState.HALF_OPEN);

        // Trial request fails: must immediately trip back to OPEN without waiting for 3 failures
        cb.recordFailure("Trial probe connection timed out", false);
        assertThat(cb.getState()).isEqualTo(CircuitState.OPEN);
        assertThat(cb.allowRequest()).isFalse();
    }
}
