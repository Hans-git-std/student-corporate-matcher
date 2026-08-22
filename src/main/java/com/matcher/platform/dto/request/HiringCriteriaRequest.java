package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request payload to define or update company hiring criteria")
public class HiringCriteriaRequest {

    @NotBlank(message = "Job title / role is mandatory")
    @Size(min = 2, max = 100, message = "Role name must be between 2 and 100 characters")
    @Schema(example = "Associate Backend Software Engineer")
    private String roleTitle;

    @Size(max = 1000, message = "Job description cannot exceed 1000 characters")
    @Schema(example = "We are seeking talented fresh graduates with strong fundamentals in CS and Java.")
    private String jobDescription;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum overall percentage cannot be less than 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Minimum overall percentage cannot exceed 100")
    @Schema(example = "70.0", description = "Minimum overall aggregate academic percentage required")
    private Double minOverallPercentage;

    @Valid
    @Schema(description = "List of required/preferred skills")
    private List<RequiredSkillEntry> requiredSkills;

    @Valid
    @Schema(description = "List of required subject mark cutoffs")
    private List<SubjectCutoffEntry> subjectCutoffs;

    public HiringCriteriaRequest() {
    }

    public HiringCriteriaRequest(String roleTitle, String jobDescription, Double minOverallPercentage, List<RequiredSkillEntry> requiredSkills, List<SubjectCutoffEntry> subjectCutoffs) {
        this.roleTitle = roleTitle;
        this.jobDescription = jobDescription;
        this.minOverallPercentage = minOverallPercentage;
        this.requiredSkills = requiredSkills;
        this.subjectCutoffs = subjectCutoffs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String roleTitle;
        private String jobDescription;
        private Double minOverallPercentage;
        private List<RequiredSkillEntry> requiredSkills;
        private List<SubjectCutoffEntry> subjectCutoffs;

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

        public Builder requiredSkills(List<RequiredSkillEntry> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public Builder subjectCutoffs(List<SubjectCutoffEntry> subjectCutoffs) {
            this.subjectCutoffs = subjectCutoffs;
            return this;
        }

        public HiringCriteriaRequest build() {
            return new HiringCriteriaRequest(roleTitle, jobDescription, minOverallPercentage, requiredSkills, subjectCutoffs);
        }
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

    public List<RequiredSkillEntry> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RequiredSkillEntry> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<SubjectCutoffEntry> getSubjectCutoffs() {
        return subjectCutoffs;
    }

    public void setSubjectCutoffs(List<SubjectCutoffEntry> subjectCutoffs) {
        this.subjectCutoffs = subjectCutoffs;
    }
}
