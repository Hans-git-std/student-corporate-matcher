package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Hiring criteria response details")
public class HiringCriteriaResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Associate Backend Software Engineer")
    private String roleTitle;

    @Schema(example = "We are seeking talented fresh graduates with strong fundamentals in CS and Java.")
    private String jobDescription;

    @Schema(example = "70.0", description = "Minimum required aggregate academic percentage")
    private Double minOverallPercentage;

    @Schema(description = "List of required skills")
    private List<RequiredSkillResponse> requiredSkills;

    @Schema(description = "List of subject mark cutoffs")
    private List<SubjectCutoffResponse> subjectCutoffs;

    public HiringCriteriaResponse() {
    }

    public HiringCriteriaResponse(Long id, String roleTitle, String jobDescription, Double minOverallPercentage, List<RequiredSkillResponse> requiredSkills, List<SubjectCutoffResponse> subjectCutoffs) {
        this.id = id;
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
        private Long id;
        private String roleTitle;
        private String jobDescription;
        private Double minOverallPercentage;
        private List<RequiredSkillResponse> requiredSkills;
        private List<SubjectCutoffResponse> subjectCutoffs;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder requiredSkills(List<RequiredSkillResponse> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public Builder subjectCutoffs(List<SubjectCutoffResponse> subjectCutoffs) {
            this.subjectCutoffs = subjectCutoffs;
            return this;
        }

        public HiringCriteriaResponse build() {
            return new HiringCriteriaResponse(id, roleTitle, jobDescription, minOverallPercentage, requiredSkills, subjectCutoffs);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<RequiredSkillResponse> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RequiredSkillResponse> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<SubjectCutoffResponse> getSubjectCutoffs() {
        return subjectCutoffs;
    }

    public void setSubjectCutoffs(List<SubjectCutoffResponse> subjectCutoffs) {
        this.subjectCutoffs = subjectCutoffs;
    }
}
