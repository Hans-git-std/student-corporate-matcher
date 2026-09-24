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
 * Priority 3 Cloud HTTPS Provider: SendGrid via Port 443.
 * Free 100 emails/day, rock-solid international deliverability.
 */
@Component
public class SendGridMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(SendGridMailProvider.class);

    private final ObjectMapper objectMapper;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("SendGrid", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.sendgrid-api-key:}")
    private String sendGridApiKey;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public SendGridMailProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "SendGrid";
    }

    @Override
    public boolean isConfigured() {
        return sendGridApiKey != null && !sendGridApiKey.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        Map<String, Object> body = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail)))),
                "from", Map.of("email", fromEmail, "name", fromName),
                "subject", subject,
                "content", List.of(Map.of("type", "text/html", "value", htmlContent))
        );

        String jsonPayload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .header("Authorization", "Bearer " + sendGridApiKey.trim())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            // SendGrid returns 202 ACCEPTED for success
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via SendGrid HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("SendGrid API HTTP %d: %s", response.statusCode(), response.body());
                boolean fatal = response.statusCode() == 401 || response.statusCode() == 403;
                circuitBreaker.recordFailure(errorMsg, fatal);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("SendGrid Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
