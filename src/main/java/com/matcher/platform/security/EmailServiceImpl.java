package com.matcher.platform.security;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.from-email:noreply@studentmatcher.com}")
    private String fromEmail;

    @Value("${app.mail.from-name:Student Corporate Matcher Platform}")
    private String fromName;

    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String recipientEmail, String otpCode) {
        // Always log OTP in server console for development / audit visibility
        log.info("==========================================================");
        log.info(" [EMAIL OTP DISPATCH] To: {}", recipientEmail);
        log.info(" [SECURITY CODE] Your 6-Digit One-Time Login Code: {}", otpCode);
        log.info(" [TTL] Valid for 5 minutes. Never share this code with anyone.");
        log.info("==========================================================");

        // If both SMTP credentials are configured, dispatch real email over network
        if (mailSender != null && smtpUsername != null && !smtpUsername.isBlank() && smtpPassword != null && !smtpPassword.isBlank()) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(fromEmail, fromName));
                helper.setTo(recipientEmail);
                helper.setSubject("Your One-Time Login Code: " + otpCode);

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

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("Real SMTP email successfully delivered to {} via {}", recipientEmail, fromEmail);
            } catch (Exception e) {
                log.error("Failed to send real SMTP email to {}. Error: {}", recipientEmail, e.getMessage());
            }
        } else {
            log.warn("SMTP username not set. Real email skipped; OTP code displayed in console above. (Set SMTP_USERNAME & SMTP_PASSWORD in .env to enable real inbox delivery).");
        }
    }
}
