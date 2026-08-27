package com.matcher.platform.entity;

import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import jakarta.persistence.*;

import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_profiles", indexes = {
        @Index(name = "idx_company_name", columnList = "company_name"),
        @Index(name = "idx_company_user_id", columnList = "user_id"),
        @Index(name = "idx_company_ver_status", columnList = "verification_status")
})
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(length = 100)
    private String industry;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private CompanyVerificationStatus verificationStatus = CompanyVerificationStatus.NOT_VERIFIED;

    @Column(name = "admin_remarks", length = 500)
    private String adminRemarks;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HiringCriteria> hiringCriteria = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public CompanyProfile() {
    }

    public CompanyProfile(Long id, User user, String companyName, String industry, String websiteUrl, String location, String description, String logoUrl, CompanyVerificationStatus verificationStatus, String adminRemarks, List<HiringCriteria> hiringCriteria, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.companyName = companyName;
        this.industry = industry;
        this.websiteUrl = websiteUrl;
        this.location = location;
        this.description = description;
        this.logoUrl = logoUrl;
        this.verificationStatus = verificationStatus != null ? verificationStatus : CompanyVerificationStatus.NOT_VERIFIED;
        this.adminRemarks = adminRemarks;
        if (hiringCriteria != null) this.hiringCriteria = hiringCriteria;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (verificationStatus == null) verificationStatus = CompanyVerificationStatus.NOT_VERIFIED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private String companyName;
        private String industry;
        private String websiteUrl;
        private String location;
        private String description;
        private String logoUrl;
        private CompanyVerificationStatus verificationStatus = CompanyVerificationStatus.NOT_VERIFIED;
        private String adminRemarks;
        private List<HiringCriteria> hiringCriteria = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
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

        public Builder adminRemarks(String adminRemarks) {
            this.adminRemarks = adminRemarks;
            return this;
        }

        public Builder hiringCriteria(List<HiringCriteria> hiringCriteria) {
            this.hiringCriteria = hiringCriteria;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CompanyProfile build() {
            return new CompanyProfile(id, user, companyName, industry, websiteUrl, location, description, logoUrl, verificationStatus, adminRemarks, hiringCriteria, createdAt, updatedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public List<HiringCriteria> getHiringCriteria() {
        return hiringCriteria;
    }

    public void setHiringCriteria(List<HiringCriteria> hiringCriteria) {
        this.hiringCriteria = hiringCriteria;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
