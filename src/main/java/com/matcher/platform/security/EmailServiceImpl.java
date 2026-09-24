package com.matcher.platform.security;

import com.matcher.platform.security.mail.MailProviderStatusDto;
import com.matcher.platform.security.mail.ResilientMailRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Enterprise-grade Email Service implementation backed by ResilientMailRouter.
 * Features:
 * - Multi-provider failover (Resend -> Brevo -> SendGrid -> SMTP).
 * - Dynamic circuit breaker preventing IP-block stalls.
 * - Always prints high-visibility security audit logs in server console.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ResilientMailRouter mailRouter;

    public EmailServiceImpl(ResilientMailRouter mailRouter) {
        this.mailRouter = mailRouter;
    }

    @Override
    public boolean sendOtpEmail(String recipientEmail, String otpCode) {
        // 1. High-visibility audit log in server console (guarantees local & cloud log visibility)
        log.info("==========================================================");
        log.info(" [EMAIL OTP DISPATCH] To: {}", recipientEmail);
        log.info(" [SECURITY CODE] Your 6-Digit One-Time Login Code: {}", otpCode);
        log.info(" [TTL] Valid for 10 minutes. Never share this code with anyone.");
        log.info("==========================================================");

        // 2. Build premium responsive HTML template
        String htmlContent = buildOtpHtmlTemplate(otpCode);
        String subject = "Your Verification Code: " + otpCode + " - Student Corporate Matcher";

        // 3. Dispatch through multi-vendor circuit breaker router
        boolean delivered = mailRouter.routeEmail(recipientEmail, subject, htmlContent);

        if (!delivered) {
            log.warn("[EMAIL NOTICE] External cloud delivery failed on all vendors. OTP is logged in console above: {}", otpCode);
        }
        return delivered;
    }

    public List<MailProviderStatusDto> getProviderHealth() {
        return mailRouter.getProvidersStatus();
    }

    private String buildOtpHtmlTemplate(String otpCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Login Verification Code</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f8fafc; padding: 40px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 540px; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03); overflow: hidden; border: 1px solid #e2e8f0;">
                                    <tr>
                                        <td style="padding: 32px 32px 24px; text-align: center; border-bottom: 1px solid #f1f5f9;">
                                            <h1 style="margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; letter-spacing: -0.5px;">Student-Corporate Matcher</h1>
                                            <p style="margin: 6px 0 0; font-size: 13px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; font-weight: 600;">Secure Identity Verification</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px; font-size: 15px; line-height: 24px; color: #334155;">Hello,</p>
                                            <p style="margin: 0 0 24px; font-size: 15px; line-height: 24px; color: #334155;">Use the following single-use verification code to complete your login. This code is valid for <strong>10 minutes</strong>.</p>
                                            
                                            <div style="background-color: #f1f5f9; border-radius: 8px; padding: 20px; text-align: center; margin: 28px 0; border: 1px dashed #cbd5e1;">
                                                <span style="font-family: 'SF Mono', Monaco, 'Courier New', monospace; font-size: 36px; font-weight: 800; letter-spacing: 10px; color: #0f172a;">%s</span>
                                            </div>

                                            <p style="margin: 0 0 12px; font-size: 13px; line-height: 20px; color: #64748b;">If you did not request this login code, you can safely disregard this email. Your account remains protected.</p>
                                            <p style="margin: 0; font-size: 13px; line-height: 20px; color: #64748b;"><strong>Security reminder:</strong> Never share this code with anyone. Platform administrators will never ask for your code.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 20px 32px; background-color: #f8fafc; border-top: 1px solid #f1f5f9; text-align: center;">
                                            <p style="margin: 0; font-size: 12px; color: #94a3b8;">&copy; 2026 Student-Corporate Matcher Platform. Enterprise Zero-Trust Security.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, otpCode);
    }
}
