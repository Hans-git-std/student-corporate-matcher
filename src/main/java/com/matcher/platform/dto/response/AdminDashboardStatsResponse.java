package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "High-level administrative system metrics")
public class AdminDashboardStatsResponse {

    @Schema(example = "1250", description = "Total registered students")
    private Long totalStudents;

    @Schema(example = "45", description = "Total registered teachers")
    private Long totalTeachers;

    @Schema(example = "82", description = "Total registered companies")
    private Long totalCompanies;

    @Schema(example = "68", description = "Total verified companies")
    private Long verifiedCompanies;

    @Schema(example = "14", description = "Companies pending administrative verification")
    private Long pendingCompanyVerifications;

    @Schema(example = "158", description = "Total student subject marks pending teacher verification")
    private Long pendingMarksVerifications;

    @Schema(example = "3420", description = "Total system matches calculated")
    private Long totalSuccessfulMatches;

    public AdminDashboardStatsResponse() {
    }

    public AdminDashboardStatsResponse(Long totalStudents, Long totalTeachers, Long totalCompanies, Long verifiedCompanies, Long pendingCompanyVerifications, Long pendingMarksVerifications, Long totalSuccessfulMatches) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalCompanies = totalCompanies;
        this.verifiedCompanies = verifiedCompanies;
        this.pendingCompanyVerifications = pendingCompanyVerifications;
        this.pendingMarksVerifications = pendingMarksVerifications;
        this.totalSuccessfulMatches = totalSuccessfulMatches;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalStudents;
        private Long totalTeachers;
        private Long totalCompanies;
        private Long verifiedCompanies;
        private Long pendingCompanyVerifications;
        private Long pendingMarksVerifications;
        private Long totalSuccessfulMatches;

        public Builder totalStudents(Long totalStudents) {
            this.totalStudents = totalStudents;
            return this;
        }

        public Builder totalTeachers(Long totalTeachers) {
            this.totalTeachers = totalTeachers;
            return this;
        }

        public Builder totalCompanies(Long totalCompanies) {
            this.totalCompanies = totalCompanies;
            return this;
        }

        public Builder verifiedCompanies(Long verifiedCompanies) {
            this.verifiedCompanies = verifiedCompanies;
            return this;
        }

        public Builder pendingCompanyVerifications(Long pendingCompanyVerifications) {
            this.pendingCompanyVerifications = pendingCompanyVerifications;
            return this;
        }

        public Builder pendingMarksVerifications(Long pendingMarksVerifications) {
            this.pendingMarksVerifications = pendingMarksVerifications;
            return this;
        }

        public Builder totalSuccessfulMatches(Long totalSuccessfulMatches) {
            this.totalSuccessfulMatches = totalSuccessfulMatches;
            return this;
        }

        public AdminDashboardStatsResponse build() {
            return new AdminDashboardStatsResponse(totalStudents, totalTeachers, totalCompanies, verifiedCompanies, pendingCompanyVerifications, pendingMarksVerifications, totalSuccessfulMatches);
        }
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(Long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public Long getTotalCompanies() {
        return totalCompanies;
    }

    public void setTotalCompanies(Long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }

    public Long getVerifiedCompanies() {
        return verifiedCompanies;
    }

    public void setVerifiedCompanies(Long verifiedCompanies) {
        this.verifiedCompanies = verifiedCompanies;
    }

    public Long getPendingCompanyVerifications() {
        return pendingCompanyVerifications;
    }

    public void setPendingCompanyVerifications(Long pendingCompanyVerifications) {
        this.pendingCompanyVerifications = pendingCompanyVerifications;
    }

    public Long getPendingMarksVerifications() {
        return pendingMarksVerifications;
    }

    public void setPendingMarksVerifications(Long pendingMarksVerifications) {
        this.pendingMarksVerifications = pendingMarksVerifications;
    }

    public Long getTotalSuccessfulMatches() {
        return totalSuccessfulMatches;
    }

    public void setTotalSuccessfulMatches(Long totalSuccessfulMatches) {
        this.totalSuccessfulMatches = totalSuccessfulMatches;
    }
}
