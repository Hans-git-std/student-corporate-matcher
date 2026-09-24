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
import java.util.Map;

/**
 * Priority 5 Cloud HTTPS Provider: Postmark via Port 443.
 * Known for world-class sub-200ms deliverability and strict zero-IP-restriction policies.
 */
@Component
public class PostmarkMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(PostmarkMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("Postmark", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.postmark-token:}")
    private String postmarkToken;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public PostmarkMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Postmark";
    }

    @Override
    public boolean isConfigured() {
        return postmarkToken != null && !postmarkToken.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        String fromFormatted = String.format("%s <%s>", fromName, fromEmail);

        Map<String, Object> body = Map.of(
                "From", fromFormatted,
                "To", toEmail,
                "Subject", subject,
                "HtmlBody", htmlContent
        );

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.postmarkapp.com/email"))
                .header("X-Postmark-Server-Token", postmarkToken.trim())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Postmark HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("Postmark API HTTP %d: %s", response.statusCode(), response.body());
                boolean fatal = response.statusCode() == 401; // Invalid server token
                circuitBreaker.recordFailure(errorMsg, fatal);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Postmark Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
