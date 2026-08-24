package com.matcher.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${app.mail.resend-api-key:}")
    private String resendApiKey;

    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        // Always log OTP in server console for development / audit visibility
        log.info("==========================================================");
        log.info(" [EMAIL OTP DISPATCH] To: {}", recipientEmail);
        log.info(" [SECURITY CODE] Your 6-Digit One-Time Login Code: {}", otpCode);
        log.info(" [TTL] Valid for 5 minutes. Never share this code with anyone.");
        log.info("==========================================================");

        String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 8px;">
                    <h2 style="color: #2563eb; margin-top: 0;">Student-Corporate Matcher Platform</h2>
                    <p>Hello,</p>
                    <p>Use the following 6-digit security code to verify your identity and log into your account:</p>
                    <div style="background: #f1f5f9; padding: 16px; border-radius: 6px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1e293b;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">This code is valid for <strong>5 minutes</strong>. If you did not request this login code, please ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">&copy; 2026 Student-Corporate Matcher Platform. All rights reserved.</p>
                </div>
                """, otpCode);

        // 1. Priority 1: Direct Brevo HTTPS REST API (Port 443 - Never blocked on Render/Cloud firewalls)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            boolean success = sendViaBrevoApi(recipientEmail, otpCode, htmlContent);
            if (success) {
                return;
            }
        }

        // 2. Priority 2: Direct Resend HTTPS REST API (Port 443)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            boolean success = sendViaResendApi(recipientEmail, otpCode, htmlContent);
            if (success) {
                return;
            }
        }

        // 3. Priority 3: SMTP / SMTPS via JavaMailSender
        if (mailSender != null && smtpUsername != null && !smtpUsername.isBlank() && smtpPassword != null && !smtpPassword.isBlank()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(fromEmail, fromName));
                helper.setTo(recipientEmail);
                helper.setSubject("Your One-Time Login Code: " + otpCode);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Real SMTP email successfully delivered to {} via {}", recipientEmail, fromEmail);
            } catch (Exception e) {
                log.error("Failed to send real SMTP email to {}. Error: {}", recipientEmail, e.getMessage());
                log.warn("Tip: Render free tier blocks outbound SMTP ports 25/587. To send real emails from Render, set BREVO_API_KEY (free 300 emails/day at brevo.com) or RESEND_API_KEY.");
            }
        } else {
            log.warn("No active email credentials configured (SMTP, BREVO_API_KEY, or RESEND_API_KEY). OTP displayed in console above.");
        }
    }

    private boolean sendViaBrevoApi(String recipientEmail, String otpCode, String htmlContent) {
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", List.of(Map.of("email", recipientEmail)),
                    "subject", "Your One-Time Login Code: " + otpCode,
                    "htmlContent", htmlContent
            );

            String jsonPayload = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Real email successfully delivered to {} via Brevo HTTPS API (Status: {})", recipientEmail, response.statusCode());
                return true;
            } else {
                log.error("Brevo HTTPS API returned error {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception during Brevo HTTPS API email delivery to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }

    private boolean sendViaResendApi(String recipientEmail, String otpCode, String htmlContent) {
        try {
            String fromFormatted = String.format("%s <%s>", fromName,
                    (fromEmail.contains("@resend.dev") || fromEmail.contains("noreply") ? "onboarding@resend.dev" : fromEmail));

            Map<String, Object> body = Map.of(
                    "from", fromFormatted,
                    "to", List.of(recipientEmail),
                    "subject", "Your One-Time Login Code: " + otpCode,
                    "html", htmlContent
            );

            String jsonPayload = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Real email successfully delivered to {} via Resend HTTPS API (Status: {})", recipientEmail, response.statusCode());
                return true;
            } else {
                log.error("Resend HTTPS API returned error {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception during Resend HTTPS API email delivery to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }
}
