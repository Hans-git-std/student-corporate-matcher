package com.matcher.platform.security;

import com.matcher.platform.entity.OtpToken;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.exception.ResourceNotFoundException;
import com.matcher.platform.repository.OtpTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
@Transactional
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 5;

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    public void generateAndSendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        // 1. Rate limiting check (max 5 requests per hour)
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentRequests = otpTokenRepository.countByEmailAndCreatedAtAfter(normalizedEmail, oneHourAgo);
        if (recentRequests >= MAX_OTP_REQUESTS_PER_HOUR) {
            throw new BadRequestException("Too many OTP requests. Please wait before requesting another code.");
        }

        // 2. Generate cryptographically strong 6-digit numeric OTP
        int otpInt = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(otpInt);
        String otpHash = hashOtp(rawOtp);

        // 3. Persist OTP token entity
        Instant expiresAt = Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES);
        OtpToken token = OtpToken.builder()
                .email(normalizedEmail)
                .otpHash(otpHash)
                .expiresAt(expiresAt)
                .attempts(0)
                .isUsed(false)
                .build();

        otpTokenRepository.save(token);

        // 4. Dispatch Email
        emailService.sendOtpEmail(normalizedEmail, rawOtp);
    }

    public boolean verifyOtp(String email, String rawOtp) {
        String normalizedEmail = email.trim().toLowerCase();

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("No active OTP request found for email: " + email));

        if (otpToken.isExpired()) {
            throw new BadRequestException("The OTP code has expired. Please request a new one.");
        }

        if (otpToken.getAttempts() >= MAX_FAILED_ATTEMPTS) {
            otpToken.setIsUsed(true); // burn token after too many failed attempts
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Too many incorrect OTP attempts. This code has been invalidated.");
        }

        String inputHash = hashOtp(rawOtp.trim());
        if (!otpToken.getOtpHash().equals(inputHash)) {
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpTokenRepository.save(otpToken);
            int remaining = MAX_FAILED_ATTEMPTS - otpToken.getAttempts();
            throw new BadRequestException("Invalid OTP code. " + remaining + " attempts remaining.");
        }

        // Mark OTP as used
        otpToken.setIsUsed(true);
        otpTokenRepository.save(otpToken);
        return true;
    }

    private String hashOtp(String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawOtp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
