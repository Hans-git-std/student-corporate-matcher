package com.matcher.platform.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminOtpSendRequest {

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Master admin password is required")
    private String password;

    private Boolean sendToRecoveryEmail = true;

    public AdminOtpSendRequest() {
    }

    public AdminOtpSendRequest(String email, String password, Boolean sendToRecoveryEmail) {
        this.email = email;
        this.password = password;
        this.sendToRecoveryEmail = sendToRecoveryEmail != null ? sendToRecoveryEmail : true;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSendToRecoveryEmail() {
        return sendToRecoveryEmail;
    }

    public void setSendToRecoveryEmail(Boolean sendToRecoveryEmail) {
        this.sendToRecoveryEmail = sendToRecoveryEmail;
    }
}
