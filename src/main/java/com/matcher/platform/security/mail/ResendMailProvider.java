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
 * Priority 1 Cloud HTTPS Provider: Resend (Port 443).
 * Zero IP restrictions, ultra-fast delivery worldwide.
 */
@Component
public class ResendMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(ResendMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("Resend", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.resend-api-key:}")
    private String resendApiKey;

    @Value("${app.mail.resend-from-email:}")
    private String resendFromEmail;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public ResendMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Resend";
    }

    @Override
    public boolean isConfigured() {
        return resendApiKey != null && !resendApiKey.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        String activeFromEmail;
        if (resendFromEmail != null && !resendFromEmail.trim().isBlank()) {
            activeFromEmail = resendFromEmail.trim();
        } else if (fromEmail.contains("@resend.dev") || fromEmail.contains("noreply")) {
            activeFromEmail = "onboarding@resend.dev";
        } else {
            activeFromEmail = fromEmail;
        }

        String fromFormatted = String.format("%s <%s>", fromName, activeFromEmail);

        Map<String, Object> body = Map.of(
                "from", fromFormatted,
                "to", List.of(toEmail),
                "subject", subject,
                "html", htmlContent
        );

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey.trim())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Resend HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("Resend API HTTP %d: %s", response.statusCode(), response.body());
                // Only 401 (invalid/revoked API key) is fatal to circuit breaker. 403 on Resend free tier indicates
                // unverified recipient on sandbox domain, which should cascade without disabling the provider globally.
                boolean fatal = response.statusCode() == 401;
                circuitBreaker.recordFailure(errorMsg, fatal);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Resend Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
