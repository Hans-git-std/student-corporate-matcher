package com.matcher.platform.security;

import com.matcher.platform.entity.OtpToken;
import com.matcher.platform.exception.BadRequestException;
import com.matcher.platform.repository.OtpTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    @Test
    @DisplayName("Should generate and send 6-digit OTP and store hashed value")
    void testGenerateAndSendOtp() {
        when(otpTokenRepository.countByEmailAndCreatedAtAfter(eq("user@platform.com"), any(Instant.class)))
                .thenReturn(0L);

        ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

        otpService.generateAndSendOtp("user@platform.com");

        verify(emailService).sendOtpEmail(eq("user@platform.com"), otpCaptor.capture());
        verify(otpTokenRepository).save(tokenCaptor.capture());

        String dispatchedOtp = otpCaptor.getValue();
        OtpToken savedToken = tokenCaptor.getValue();

        assertThat(dispatchedOtp).hasSize(6);
        assertThat(savedToken.getEmail()).isEqualTo("user@platform.com");
        assertThat(savedToken.getOtpHash()).isNotEmpty();
        assertThat(savedToken.getIsUsed()).isFalse();
    }

    @Test
    @DisplayName("Should block OTP generation when rate limit is exceeded")
    void testRateLimitExceeded() {
        when(otpTokenRepository.countByEmailAndCreatedAtAfter(eq("user@platform.com"), any(Instant.class)))
                .thenReturn(5L); // max 5

        assertThatThrownBy(() -> otpService.generateAndSendOtp("user@platform.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many OTP requests");
    }

    @Test
    @DisplayName("Should verify valid OTP and mark token as used")
    void testVerifyValidOtp() {
        // Hash for "123456"
        // SHA-256("123456") = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
        String hash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

        OtpToken token = OtpToken.builder()
                .id(1L)
                .email("user@platform.com")
                .otpHash(hash)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .attempts(0)
                .isUsed(false)
                .build();

        when(otpTokenRepository.findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc("user@platform.com"))
                .thenReturn(Optional.of(token));

        boolean isValid = otpService.verifyOtp("user@platform.com", "123456");

        assertThat(isValid).isTrue();
        assertThat(token.getIsUsed()).isTrue();
        verify(otpTokenRepository).save(token);
    }
}
