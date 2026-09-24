package com.matcher.platform.security;

public interface EmailService {
    boolean sendOtpEmail(String recipientEmail, String otpCode);
}
