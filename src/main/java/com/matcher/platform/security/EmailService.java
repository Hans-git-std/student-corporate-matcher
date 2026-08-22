package com.matcher.platform.security;

public interface EmailService {
    void sendOtpEmail(String recipientEmail, String otpCode);
}
