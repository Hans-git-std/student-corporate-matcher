package com.matcher.platform.dto.response;

import java.time.Instant;

public class ActiveOtpResponse {

    private Long id;
    private String email;
    private String otpCode;
    private Instant createdAt;
    private Instant expiresAt;
    private Long remainingSeconds;
    private Boolean isUsed;
    private String status;

    public ActiveOtpResponse() {
    }

    public ActiveOtpResponse(Long id, String email, String otpCode, Instant createdAt, Instant expiresAt, Long remainingSeconds, Boolean isUsed, String status) {
        this.id = id;
        this.email = email;
        this.otpCode = otpCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.remainingSeconds = remainingSeconds;
        this.isUsed = isUsed;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
