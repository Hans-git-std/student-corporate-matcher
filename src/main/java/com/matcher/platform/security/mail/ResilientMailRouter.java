package com.matcher.platform.security.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Intelligent Multi-Vendor Email Router.
 * Features:
 * - Dynamic priority-based routing across multiple vendors (Resend, Brevo, SendGrid, SMTP).
 * - Automatic circuit breaking to skip degraded/IP-blocked providers in 0ms without latency penalties.
 * - Real-time failover to subsequent vendors if the active vendor fails.
 * - Comprehensive health & telemetry tracking.
 */
@Component
public class ResilientMailRouter {

    private static final Logger log = LoggerFactory.getLogger(ResilientMailRouter.class);

    private final List<MailProvider> providers;

    public ResilientMailRouter(List<MailProvider> providers) {
        // Sort providers by priority ascending (1 = highest, 2 = secondary, etc.)
        this.providers = providers.stream()
                .sorted(Comparator.comparingInt(MailProvider::getPriority))
                .toList();

        log.info("Initialized ResilientMailRouter with {} providers in order: {}",
                this.providers.size(),
                this.providers.stream().map(p -> p.getProviderName() + " (Priority " + p.getPriority() + ", Configured: " + p.isConfigured() + ")").toList());
    }

    /**
     * Dispatches an email using the healthiest available provider with automatic cascading failover.
     *
     * @return true if successfully delivered by any provider
     */
    public boolean routeEmail(String toEmail, String subject, String htmlContent) {
        int configuredCount = 0;

        for (MailProvider provider : providers) {
            if (!provider.isConfigured()) {
                continue;
            }
            configuredCount++;

            ProviderCircuitBreaker cb = provider.getCircuitBreaker();
            if (!cb.allowRequest()) {
                log.warn("[CIRCUIT BREAKER] Skipping {} for {} - State is OPEN (Reason: {})",
                        provider.getProviderName(), toEmail, cb.getLastErrorMessage());
                continue;
            }

            try {
                log.info("[ROUTER] Attempting dispatch to {} via {}", toEmail, provider.getProviderName());
                boolean sent = provider.sendEmail(toEmail, subject, htmlContent);
                if (sent) {
                    log.info("[ROUTER SUCCESS] Email successfully delivered to {} via {}", toEmail, provider.getProviderName());
                    return true;
                } else {
                    log.warn("[AUTO-FAILOVER] Provider {} could not deliver to {}. Cascading to next available vendor...",
                            provider.getProviderName(), toEmail);
                }
            } catch (Exception e) {
                log.warn("[AUTO-FAILOVER] Exception during dispatch via {} to {}: {}. Cascading to next available vendor...",
                        provider.getProviderName(), toEmail, e.getMessage());
            }
        }

        if (configuredCount == 0) {
            log.warn("[MAIL ROUTER] No real email provider configured. Verification codes logged in server audit console.");
            return false;
        }

        log.error("[CRITICAL MAIL FAILURE] All configured email providers failed or are OPEN! Recipient: {}", toEmail);
        return false;
    }

    /**
     * Returns a snapshot of health and performance metrics for all registered providers.
     */
    public List<MailProviderStatusDto> getProvidersStatus() {
        return providers.stream()
                .map(p -> {
                    ProviderCircuitBreaker cb = p.getCircuitBreaker();
                    return new MailProviderStatusDto(
                            p.getProviderName(),
                            p.isConfigured(),
                            p.getPriority(),
                            cb.getState(),
                            cb.getTotalRequests(),
                            cb.getSuccessCount(),
                            cb.getFailureCount(),
                            cb.getLastLatencyMs(),
                            cb.getLastErrorMessage(),
                            cb.getLastSuccessTime(),
                            cb.getLastFailureTime()
                    );
                })
                .toList();
    }
}
