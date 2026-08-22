package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload to verify OTP and authenticate")
public class OtpVerifyRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Invalid email format")
    @Schema(example = "student@university.edu")
    private String email;

    @NotBlank(message = "OTP code is mandatory")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit number")
    @Schema(example = "123456", description = "6-digit OTP code received via email")
    private String otp;

    public OtpVerifyRequest() {
    }

    public OtpVerifyRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String otp;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder otp(String otp) {
            this.otp = otp;
            return this;
        }

        public OtpVerifyRequest build() {
            return new OtpVerifyRequest(email, otp);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
