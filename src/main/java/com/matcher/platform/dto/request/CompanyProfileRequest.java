package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to update company profile")
public class CompanyProfileRequest {

    @NotBlank(message = "Company name is mandatory")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    @Schema(example = "Acme Technologies Inc.")
    private String companyName;

    @Size(max = 100, message = "Industry name must not exceed 100 characters")
    @Schema(example = "Enterprise Software & Cloud Computing")
    private String industry;

    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid website URL")
    @Schema(example = "https://acmetech.com")
    private String websiteUrl;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(example = "San Francisco, CA, USA")
    private String location;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(example = "Leading provider of cloud infrastructure and developer automation tooling.")
    private String description;

    @Schema(example = "https://acmetech.com/logo.png")
    private String logoUrl;

    public CompanyProfileRequest() {
    }

    public CompanyProfileRequest(String companyName, String industry, String websiteUrl, String location, String description, String logoUrl) {
        this.companyName = companyName;
        this.industry = industry;
        this.websiteUrl = websiteUrl;
        this.location = location;
        this.description = description;
        this.logoUrl = logoUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String companyName;
        private String industry;
        private String websiteUrl;
        private String location;
        private String description;
        private String logoUrl;

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

        public CompanyProfileRequest build() {
            return new CompanyProfileRequest(companyName, industry, websiteUrl, location, description, logoUrl);
        }
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
}
