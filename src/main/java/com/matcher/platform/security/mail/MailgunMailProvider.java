package com.matcher.platform.security.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Priority 4 Cloud HTTPS Provider: Mailgun via Port 443.
 * Industry standard REST API with fast delivery and high reliability.
 */
@Component
public class MailgunMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(MailgunMailProvider.class);

    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("Mailgun", 2, 5 * 60 * 1000L);
    private final HttpClient httpClient;

    @Value("${app.mail.mailgun-api-key:}")
    private String mailgunApiKey;

    @Value("${app.mail.mailgun-domain:}")
    private String mailgunDomain;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public MailgunMailProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public String getProviderName() {
        return "Mailgun";
    }

    @Override
    public boolean isConfigured() {
        return mailgunApiKey != null && !mailgunApiKey.trim().isBlank()
                && mailgunDomain != null && !mailgunDomain.trim().isBlank();
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
        String fromFormatted = String.format("%s <%s>", fromName, fromEmail);

        String formBody = "from=" + URLEncoder.encode(fromFormatted, StandardCharsets.UTF_8)
                + "&to=" + URLEncoder.encode(toEmail, StandardCharsets.UTF_8)
                + "&subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
                + "&html=" + URLEncoder.encode(htmlContent, StandardCharsets.UTF_8);

        String authHeader = "Basic " + Base64.getEncoder().encodeToString(("api:" + mailgunApiKey.trim()).getBytes(StandardCharsets.UTF_8));
        String endpoint = "https://api.mailgun.net/v3/" + mailgunDomain.trim() + "/messages";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[MAIL DISPATCH] Delivered to {} via Mailgun HTTPS API (Status: {}, Latency: {}ms)",
                        toEmail, response.statusCode(), latency);
                circuitBreaker.recordSuccess(latency);
                return true;
            } else {
                String errorMsg = String.format("Mailgun API HTTP %d: %s", response.statusCode(), response.body());
                boolean fatal = response.statusCode() == 401; // Invalid API credentials
                circuitBreaker.recordFailure(errorMsg, fatal);
                log.warn("[MAIL DISPATCH FAILURE] {}", errorMsg);
                return false;
            }
        } catch (Exception e) {
            circuitBreaker.recordFailure("Mailgun Connection Error: " + e.getMessage(), false);
            throw e;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
