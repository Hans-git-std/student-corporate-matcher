package com.matcher.platform.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email", columnList = "email"),
        @Index(name = "idx_otp_created", columnList = "created_at")
})
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "otp_hash", nullable = false, length = 128)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public OtpToken() {
    }

    public OtpToken(String email, String otpHash, Instant expiresAt) {
        this.email = email;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.isUsed = false;
        this.createdAt = Instant.now();
    }

    public OtpToken(Long id, String email, String otpHash, Instant expiresAt, Integer attempts, Boolean isUsed, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.attempts = attempts != null ? attempts : 0;
        this.isUsed = isUsed != null ? isUsed : false;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String otpHash;
        private Instant expiresAt;
        private Integer attempts = 0;
        private Boolean isUsed = false;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder otpHash(String otpHash) {
            this.otpHash = otpHash;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder attempts(Integer attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder isUsed(Boolean isUsed) {
            this.isUsed = isUsed;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OtpToken build() {
            return new OtpToken(id, email, otpHash, expiresAt, attempts, isUsed, createdAt);
        }
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

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
