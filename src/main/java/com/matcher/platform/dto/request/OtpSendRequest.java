package com.matcher.platform.dto.request;

import com.matcher.platform.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to trigger an Email OTP")
public class OtpSendRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Invalid email format")
    @Schema(example = "student@university.edu", description = "User's registered email address")
    private String email;

    @Schema(example = "ROLE_STUDENT", description = "Optional target role when registering a new account")
    private RoleType role = RoleType.ROLE_STUDENT;

    public OtpSendRequest() {
    }

    public OtpSendRequest(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.role = RoleType.ROLE_STUDENT;
    }

    public OtpSendRequest(String email, RoleType role) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.role = role != null ? role : RoleType.ROLE_STUDENT;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private RoleType role = RoleType.ROLE_STUDENT;

        public Builder email(String email) {
            this.email = email != null ? email.trim().toLowerCase() : null;
            return this;
        }

        public Builder role(RoleType role) {
            this.role = role;
            return this;
        }

        public OtpSendRequest build() {
            return new OtpSendRequest(email, role);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
