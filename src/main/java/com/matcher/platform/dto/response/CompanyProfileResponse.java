package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Company profile with administrative verification status and hiring criteria")
public class CompanyProfileResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "recruiting@acmetech.com")
    private String email;

    @Schema(example = "Acme Technologies Inc.")
    private String companyName;

    @Schema(example = "Enterprise Software & Cloud Computing")
    private String industry;

    @Schema(example = "https://acmetech.com")
    private String websiteUrl;

    @Schema(example = "San Francisco, CA, USA")
    private String location;

    @Schema(example = "Leading provider of cloud infrastructure and developer automation tooling.")
    private String description;

    @Schema(example = "https://acmetech.com/logo.png")
    private String logoUrl;

    @Schema(example = "VERIFIED", description = "NOT_VERIFIED, VERIFIED, REJECTED")
    private CompanyVerificationStatus verificationStatus;

    @Schema(example = "Not Verified", description = "User-facing verification badge")
    private String verificationBadge;

    @Schema(description = "List of active hiring criteria posted by company")
    private List<HiringCriteriaResponse> hiringCriteria;

    public CompanyProfileResponse() {
    }

    public CompanyProfileResponse(Long id, String email, String companyName, String industry, String websiteUrl, String location, String description, String logoUrl, CompanyVerificationStatus verificationStatus, String verificationBadge, List<HiringCriteriaResponse> hiringCriteria) {
        this.id = id;
        this.email = email;
        this.companyName = companyName;
        this.industry = industry;
        this.websiteUrl = websiteUrl;
        this.location = location;
        this.description = description;
        this.logoUrl = logoUrl;
        this.verificationStatus = verificationStatus;
        this.verificationBadge = verificationBadge;
        this.hiringCriteria = hiringCriteria;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String companyName;
        private String industry;
        private String websiteUrl;
        private String location;
        private String description;
        private String logoUrl;
        private CompanyVerificationStatus verificationStatus;
        private String verificationBadge;
        private List<HiringCriteriaResponse> hiringCriteria;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder websiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        public Builder verificationStatus(CompanyVerificationStatus verificationStatus) {
            this.verificationStatus = verificationStatus;
            return this;
        }

        public Builder verificationBadge(String verificationBadge) {
            this.verificationBadge = verificationBadge;
            return this;
        }

        public Builder hiringCriteria(List<HiringCriteriaResponse> hiringCriteria) {
            this.hiringCriteria = hiringCriteria;
            return this;
        }

        public CompanyProfileResponse build() {
            return new CompanyProfileResponse(id, email, companyName, industry, websiteUrl, location, description, logoUrl, verificationStatus, verificationBadge, hiringCriteria);
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public CompanyVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(CompanyVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationBadge() {
        return verificationBadge;
    }

    public void setVerificationBadge(String verificationBadge) {
        this.verificationBadge = verificationBadge;
    }

    public List<HiringCriteriaResponse> getHiringCriteria() {
        return hiringCriteria;
    }

    public void setHiringCriteria(List<HiringCriteriaResponse> hiringCriteria) {
        this.hiringCriteria = hiringCriteria;
    }
}
