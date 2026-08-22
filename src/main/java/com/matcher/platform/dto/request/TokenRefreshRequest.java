package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to rotate refresh token and obtain a new access token")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh token is mandatory")
    @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", description = "Valid, non-expired refresh token")
    private String refreshToken;

    public TokenRefreshRequest() {
    }

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String refreshToken;

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenRefreshRequest build() {
            return new TokenRefreshRequest(refreshToken);
        }
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
