package com.matcher.platform.dto.request;

import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin payload to update company verification status")
public class CompanyStatusUpdateRequest {

    @NotNull(message = "Verification status is mandatory")
    @Schema(example = "VERIFIED", description = "NOT_VERIFIED, VERIFIED, REJECTED")
    private CompanyVerificationStatus status;

    @Schema(example = "Documents verified and company legitimate", description = "Admin remarks")
    private String adminRemarks;

    public CompanyStatusUpdateRequest() {
    }

    public CompanyStatusUpdateRequest(CompanyVerificationStatus status, String adminRemarks) {
        this.status = status;
        this.adminRemarks = adminRemarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CompanyVerificationStatus status;
        private String adminRemarks;

        public Builder status(CompanyVerificationStatus status) {
            this.status = status;
            return this;
        }

        public Builder adminRemarks(String adminRemarks) {
            this.adminRemarks = adminRemarks;
            return this;
        }

        public CompanyStatusUpdateRequest build() {
            return new CompanyStatusUpdateRequest(status, adminRemarks);
        }
    }

    public CompanyVerificationStatus getStatus() {
        return status;
    }

    public void setStatus(CompanyVerificationStatus status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }
}
