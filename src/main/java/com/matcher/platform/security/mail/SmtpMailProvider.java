package com.matcher.platform.security.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Priority 4 Fallback Provider: Traditional SMTP / SMTPS via JavaMailSender.
 * Note: Render free tier blocks outbound SMTP ports 25/587. Guarded by strict circuit breaker.
 */
@Component
public class SmtpMailProvider implements MailProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailProvider.class);

    private final JavaMailSender mailSender;
    private final ProviderCircuitBreaker circuitBreaker = new ProviderCircuitBreaker("SMTP", 1, 10 * 60 * 1000L);

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public SmtpMailProvider(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    @Override
    public boolean isConfigured() {
        return mailSender != null && smtpUsername != null && !smtpUsername.trim().isBlank()
                && smtpPassword != null && !smtpPassword.trim().isBlank();
    }

    @Override
    public int getPriority() {
        return 6; // Lowest priority behind HTTPS REST APIs
    }

    @Override
    public boolean sendEmail(String toEmail, String subject, String htmlContent) throws Exception {
        if (!isConfigured()) {
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            long latency = System.currentTimeMillis() - startTime;

            log.info("[MAIL DISPATCH] Delivered to {} via SMTP (Latency: {}ms)", toEmail, latency);
            circuitBreaker.recordSuccess(latency);
            return true;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            String errorMsg = "SMTP Failure (" + latency + "ms): " + e.getMessage();
            boolean isPortBlock = errorMsg.contains("SocketTimeoutException") || errorMsg.contains("MailConnectException");

            log.warn("[SMTP FAILURE] {}. Tripping SMTP circuit breaker.", errorMsg);
            // If connection timed out (e.g. Render port 587 block), trip immediately
            circuitBreaker.recordFailure(errorMsg, isPortBlock);
            return false;
        }
    }

    @Override
    public ProviderCircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
