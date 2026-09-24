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
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Priority 1 Cloud HTTPS Provider: Mailjet via Port 443.
 * 200 free emails/day (6,000/month) with zero IP restrictions.
 * Allows sending to ANY recipient email address in the world.
 */
@Component
public class MailjetMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(MailjetMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("Mailjet", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.mailjet-api-key:}")
    private String mailjetApiKey;

    @Value("${app.mail.mailjet-secret-key:}")
    private String mailjetSecretKey;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public MailjetMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Mailjet";
    }

    @Override
    public boolean isConfigured() {
        return mailjetApiKey != null && !mailjetApiKey.trim().isBlank()
                && mailjetSecretKey != null && !mailjetSecretKey.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority for universal deliverability
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        Map<String, Object> message = Map.of(
                "From", Map.of("Email", fromEmail.trim(), "Name", fromName.trim()),
                "To", List.of(Map.of("Email", toEmail.trim())),
                "Subject", subject,
                "HTMLPart", htmlContent
        );

        Map<String, Object> body = Map.of("Messages", List.of(message));
        String jsonPayload = objectMapper.writeValueAsString(body);

        String rawAuth = mailjetApiKey.trim() + ":" + mailjetSecretKey.trim();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(rawAuth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mailjet.com/v3.1/send"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Mailjet HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("Mailjet API HTTP %d: %s", response.statusCode(), response.body());
                boolean fatal = response.statusCode() == 401; // Invalid credentials
                circuitBreaker.recordFailure(errorMsg, fatal);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Mailjet Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
