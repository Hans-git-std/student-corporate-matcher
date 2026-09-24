package com.matcher.platform.security.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Priority 2 Cloud HTTPS Provider: Brevo (Sendinblue) via Port 443.
 * Includes immediate IP-lock detection & auto-trip circuit breaker.
 */
@Component
public class BrevoMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(BrevoMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("Brevo", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public BrevoMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Brevo";
    }

    @Override
    public boolean isConfigured() {
        return brevoApiKey != null && !brevoApiKey.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", brevoApiKey.trim())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Brevo HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String responseBody = response.body();
                boolean isIpRestriction = response.statusCode() == 401 && responseBody.contains("unrecognised IP address");
                String errorMsg = String.format("Brevo API HTTP %d: %s", response.statusCode(), responseBody);

                if (isIpRestriction) {
                    log.error("[BREVO IP RESTRICTION DETECTED] Brevo blocked cloud dynamic IP! Tripping circuit breaker to OPEN immediately: {}", responseBody);
                } else {
                    log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                }

                // If IP restriction or 401/403, trip immediately to OPEN
                boolean fatal = isIpRestriction || response.statusCode() == 401 || response.statusCode() == 403;
                circuitBreaker.recordFailure(errorMsg, fatal);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Brevo Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
