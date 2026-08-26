package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for company self-registration")
public class CompanyRegisterRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "Invalid email format")
    @Schema(example = "recruiting@acmetech.com")
    private String email;

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

    public CompanyRegisterRequest() {
    }

    public CompanyRegisterRequest(String email, String companyName, String industry, String websiteUrl, String location, String description) {
        this(email, companyName, industry, websiteUrl, location, description, null);
    }

    public CompanyRegisterRequest(String email, String companyName, String industry, String websiteUrl, String location, String description, String logoUrl) {
        this.email = email;
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
        private String email;
        private String companyName;
        private String industry;
        private String websiteUrl;
        private String location;
        private String description;
        private String logoUrl;

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

        public CompanyRegisterRequest build() {
            return new CompanyRegisterRequest(email, companyName, industry, websiteUrl, location, description, logoUrl);
        }
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
}
