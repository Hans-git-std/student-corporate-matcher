package com.matcher.platform.security.mail;

/**
 * Common contract for all email dispatch vendors (Resend, Brevo, SendGrid, SMTP, etc.).
 */
public interface MailProvider {

    /**
     * Unique identifier for this provider.
     */
    String getProviderName();

    /**
     * Checks if credentials / configuration are active for this provider.
     */
    boolean isConfigured();

    /**
     * Priority rank in failover chain (1 = highest, 2 = secondary, etc.)
     */
    int getPriority();

    /**
     * Dispatches an email message. Returns true if delivered successfully.
     * Throws an exception or returns false upon failure.
     */
    boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception;

    /**
     * Returns the circuit breaker guarding this provider.
     */
    ProviderCircuitBreaker getCircuitBreaker();
}
