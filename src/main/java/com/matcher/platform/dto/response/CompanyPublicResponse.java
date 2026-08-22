package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Publicly viewable company profile and active openings")
public class CompanyPublicResponse {

    @Schema(example = "1")
    private Long id;

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

    @Schema(example = "VERIFIED")
    private CompanyVerificationStatus verificationStatus;

    @Schema(example = "Not Verified", description = "Shows 'Not Verified' if company self-registered and unapproved")
    private String verificationBadge;

    @Schema(description = "Publicly visible hiring openings")
    private List<HiringCriteriaResponse> activeCriteria;

    public CompanyPublicResponse() {
    }

    public CompanyPublicResponse(Long id, String companyName, String industry, String websiteUrl, String location, String description, String logoUrl, CompanyVerificationStatus verificationStatus, String verificationBadge, List<HiringCriteriaResponse> activeCriteria) {
        this.id = id;
        this.companyName = companyName;
        this.industry = industry;
        this.websiteUrl = websiteUrl;
        this.location = location;
        this.description = description;
        this.logoUrl = logoUrl;
        this.verificationStatus = verificationStatus;
        this.verificationBadge = verificationBadge;
        this.activeCriteria = activeCriteria;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String companyName;
        private String industry;
        private String websiteUrl;
        private String location;
        private String description;
        private String logoUrl;
        private CompanyVerificationStatus verificationStatus;
        private String verificationBadge;
        private List<HiringCriteriaResponse> activeCriteria;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder activeCriteria(List<HiringCriteriaResponse> activeCriteria) {
            this.activeCriteria = activeCriteria;
            return this;
        }

        public CompanyPublicResponse build() {
            return new CompanyPublicResponse(id, companyName, industry, websiteUrl, location, description, logoUrl, verificationStatus, verificationBadge, activeCriteria);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<HiringCriteriaResponse> getActiveCriteria() {
        return activeCriteria;
    }

    public void setActiveCriteria(List<HiringCriteriaResponse> activeCriteria) {
        this.activeCriteria = activeCriteria;
    }
}
