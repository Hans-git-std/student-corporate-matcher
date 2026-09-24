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
 * Priority 1 Google Apps Script Gateway Provider via Port 443 HTTPS.
 * Sends directly through Google's native Gmail infrastructure using your personal Gmail account.
 * Features:
 * - Completely immune to Render's port 25/587 firewall blocks.
 * - Zero third-party account suspensions or IP whitelisting traps.
 * - Delivers natively to Primary inbox with Google DKIM/SPF signatures.
 */
@Component
public class GoogleGatewayMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleGatewayMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("GoogleGateway", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.google-gateway-url:}")
    private String googleGatewayUrl;

    public GoogleGatewayMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getProviderName() {
        return "GoogleGateway";
    }

    @Override
    public boolean isConfigured() {
        return googleGatewayUrl != null && !googleGatewayUrl.trim().isBlank()
                && googleGatewayUrl.trim().startsWith("https://script.google.com");
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority if configured
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        Map<String, Object> body = Map.of(
                "to", toEmail.trim(),
                "subject", subject,
                "html", htmlContent
        );

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(googleGatewayUrl.trim()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Google Gateway (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("Google Gateway HTTP %d: %s", response.statusCode(), response.body());
                circuitBreaker.recordFailure(errorMsg, false);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Google Gateway Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
