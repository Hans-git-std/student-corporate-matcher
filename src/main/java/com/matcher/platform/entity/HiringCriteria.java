package com.matcher.platform.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hiring_criteria", indexes = {
        @Index(name = "idx_crit_company_id", columnList = "company_id"),
        @Index(name = "idx_crit_is_active", columnList = "is_active")
})
public class HiringCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyProfile company;

    @Column(name = "role_title", nullable = false, length = 100)
    private String roleTitle;

    @Column(name = "job_description", length = 1000)
    private String jobDescription;

    @Column(name = "min_overall_percentage")
    private Double minOverallPercentage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CriteriaRequiredSkill> requiredSkills = new ArrayList<>();

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CriteriaSubjectCutoff> subjectCutoffs = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public HiringCriteria() {
    }

    public HiringCriteria(Long id, CompanyProfile company, String roleTitle, String jobDescription, Double minOverallPercentage, Boolean isActive, List<CriteriaRequiredSkill> requiredSkills, List<CriteriaSubjectCutoff> subjectCutoffs, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.company = company;
        this.roleTitle = roleTitle;
        this.jobDescription = jobDescription;
        this.minOverallPercentage = minOverallPercentage;
        this.isActive = isActive != null ? isActive : true;
        if (requiredSkills != null) this.requiredSkills = requiredSkills;
        if (subjectCutoffs != null) this.subjectCutoffs = subjectCutoffs;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (isActive == null) isActive = true;
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
        private CompanyProfile company;
        private String roleTitle;
        private String jobDescription;
        private Double minOverallPercentage;
        private Boolean isActive = true;
        private List<CriteriaRequiredSkill> requiredSkills = new ArrayList<>();
        private List<CriteriaSubjectCutoff> subjectCutoffs = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder company(CompanyProfile company) {
            this.company = company;
            return this;
        }

        public Builder roleTitle(String roleTitle) {
            this.roleTitle = roleTitle;
            return this;
        }

        public Builder jobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
            return this;
        }

        public Builder minOverallPercentage(Double minOverallPercentage) {
            this.minOverallPercentage = minOverallPercentage;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder requiredSkills(List<CriteriaRequiredSkill> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public Builder subjectCutoffs(List<CriteriaSubjectCutoff> subjectCutoffs) {
            this.subjectCutoffs = subjectCutoffs;
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

        public HiringCriteria build() {
            return new HiringCriteria(id, company, roleTitle, jobDescription, minOverallPercentage, isActive, requiredSkills, subjectCutoffs, createdAt, updatedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CompanyProfile getCompany() {
        return company;
    }

    public void setCompany(CompanyProfile company) {
        this.company = company;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Double getMinOverallPercentage() {
        return minOverallPercentage;
    }

    public void setMinOverallPercentage(Double minOverallPercentage) {
        this.minOverallPercentage = minOverallPercentage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<CriteriaRequiredSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<CriteriaRequiredSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<CriteriaSubjectCutoff> getSubjectCutoffs() {
        return subjectCutoffs;
    }

    public void setSubjectCutoffs(List<CriteriaSubjectCutoff> subjectCutoffs) {
        this.subjectCutoffs = subjectCutoffs;
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
