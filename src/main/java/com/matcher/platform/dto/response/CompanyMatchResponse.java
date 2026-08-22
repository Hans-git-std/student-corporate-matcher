package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.MatchType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Calculated company match for a student, supporting both Strict and Relaxed Weighted modes with actionable feedback")
public class CompanyMatchResponse {

    @Schema(example = "1")
    private Long companyId;

    @Schema(example = "Acme Technologies Inc.")
    private String companyName;

    @Schema(example = "https://acmetech.com/logo.png")
    private String logoUrl;

    @Schema(example = "San Francisco, CA, USA")
    private String location;

    @Schema(example = "VERIFIED")
    private CompanyVerificationStatus companyVerificationStatus;

    @Schema(example = "Associate Backend Software Engineer")
    private String roleTitle;

    @Schema(example = "87.5", description = "Match score percentage (0-100%)")
    private Double matchScore;

    @Schema(example = "STRICT", description = "STRICT (meets all criteria) or RELAXED_WEIGHTED (closest fit)")
    private MatchType matchType;

    @Schema(example = "false", description = "True if any evaluated mark is pending teacher verification")
    private Boolean isVerificationPending;

    @Schema(example = "Verification by Teacher is Required", description = "Attached verification reminder remark")
    private String verificationRemark;

    @Schema(description = "Skills successfully matched with company requirements")
    private List<String> matchedSkills;

    @Schema(description = "Skills required by company but missing in student profile")
    private List<String> missingSkills;

    @Schema(description = "Academic subject score gaps with actionable deficit notices")
    private List<SubjectGapDetail> subjectGaps;

    @Schema(example = "A 10.0 score more is required in Data Structures & Algorithms", description = "Primary actionable academic gap remark")
    private String academicGapSummary;

    public CompanyMatchResponse() {
    }

    public CompanyMatchResponse(Long companyId, String companyName, String logoUrl, String location, CompanyVerificationStatus companyVerificationStatus, String roleTitle, Double matchScore, MatchType matchType, Boolean isVerificationPending, String verificationRemark, List<String> matchedSkills, List<String> missingSkills, List<SubjectGapDetail> subjectGaps, String academicGapSummary) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.logoUrl = logoUrl;
        this.location = location;
        this.companyVerificationStatus = companyVerificationStatus;
        this.roleTitle = roleTitle;
        this.matchScore = matchScore;
        this.matchType = matchType;
        this.isVerificationPending = isVerificationPending;
        this.verificationRemark = verificationRemark;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.subjectGaps = subjectGaps;
        this.academicGapSummary = academicGapSummary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long companyId;
        private String companyName;
        private String logoUrl;
        private String location;
        private CompanyVerificationStatus companyVerificationStatus;
        private String roleTitle;
        private Double matchScore;
        private MatchType matchType;
        private Boolean isVerificationPending;
        private String verificationRemark;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<SubjectGapDetail> subjectGaps;
        private String academicGapSummary;

        public Builder companyId(Long companyId) {
            this.companyId = companyId;
            return this;
        }

        public Builder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder companyVerificationStatus(CompanyVerificationStatus companyVerificationStatus) {
            this.companyVerificationStatus = companyVerificationStatus;
            return this;
        }

        public Builder roleTitle(String roleTitle) {
            this.roleTitle = roleTitle;
            return this;
        }

        public Builder matchScore(Double matchScore) {
            this.matchScore = matchScore;
            return this;
        }

        public Builder matchType(MatchType matchType) {
            this.matchType = matchType;
            return this;
        }

        public Builder isVerificationPending(Boolean isVerificationPending) {
            this.isVerificationPending = isVerificationPending;
            return this;
        }

        public Builder verificationRemark(String verificationRemark) {
            this.verificationRemark = verificationRemark;
            return this;
        }

        public Builder matchedSkills(List<String> matchedSkills) {
            this.matchedSkills = matchedSkills;
            return this;
        }

        public Builder missingSkills(List<String> missingSkills) {
            this.missingSkills = missingSkills;
            return this;
        }

        public Builder subjectGaps(List<SubjectGapDetail> subjectGaps) {
            this.subjectGaps = subjectGaps;
            return this;
        }

        public Builder academicGapSummary(String academicGapSummary) {
            this.academicGapSummary = academicGapSummary;
            return this;
        }

        public CompanyMatchResponse build() {
            return new CompanyMatchResponse(companyId, companyName, logoUrl, location, companyVerificationStatus, roleTitle, matchScore, matchType, isVerificationPending, verificationRemark, matchedSkills, missingSkills, subjectGaps, academicGapSummary);
        }
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public CompanyVerificationStatus getCompanyVerificationStatus() {
        return companyVerificationStatus;
    }

    public void setCompanyVerificationStatus(CompanyVerificationStatus companyVerificationStatus) {
        this.companyVerificationStatus = companyVerificationStatus;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Boolean getIsVerificationPending() {
        return isVerificationPending;
    }

    public void setIsVerificationPending(Boolean isVerificationPending) {
        this.isVerificationPending = isVerificationPending;
    }

    public String getVerificationRemark() {
        return verificationRemark;
    }

    public void setVerificationRemark(String verificationRemark) {
        this.verificationRemark = verificationRemark;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<SubjectGapDetail> getSubjectGaps() {
        return subjectGaps;
    }

    public void setSubjectGaps(List<SubjectGapDetail> subjectGaps) {
        this.subjectGaps = subjectGaps;
    }

    public String getAcademicGapSummary() {
        return academicGapSummary;
    }

    public void setAcademicGapSummary(String academicGapSummary) {
        this.academicGapSummary = academicGapSummary;
    }
}
