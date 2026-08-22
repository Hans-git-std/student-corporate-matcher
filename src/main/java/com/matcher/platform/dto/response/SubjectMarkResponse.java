package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Subject mark details with dual self-reported and teacher verification metadata")
public class SubjectMarkResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Data Structures & Algorithms")
    private String subjectName;

    @Schema(example = "88.5", description = "Self-reported mark out of 100")
    private Double selfReportedMarks;

    @Schema(example = "90.0", description = "Official teacher-verified mark out of 100")
    private Double verifiedMarks;

    @Schema(example = "true", description = "Whether mark has been confirmed by faculty")
    private Boolean isVerified;

    @Schema(example = "Semester 4")
    private String semester;

    @Schema(example = "EMP-FAC-1002", description = "Employee ID of verifying teacher")
    private String verifiedByTeacherId;

    @Schema(example = "Dr. Alan Turing", description = "Name of verifying faculty member")
    private String verifiedByTeacherName;

    @Schema(description = "Timestamp when verification was performed")
    private Instant verifiedAt;

    @Schema(example = "Verification by Teacher is Required", description = "Verification reminder remark")
    private String verificationRemark;

    public SubjectMarkResponse() {
    }

    public SubjectMarkResponse(Long id, String subjectName, Double selfReportedMarks, Double verifiedMarks, Boolean isVerified, String semester, String verifiedByTeacherId, String verifiedByTeacherName, Instant verifiedAt, String verificationRemark) {
        this.id = id;
        this.subjectName = subjectName;
        this.selfReportedMarks = selfReportedMarks;
        this.verifiedMarks = verifiedMarks;
        this.isVerified = isVerified;
        this.semester = semester;
        this.verifiedByTeacherId = verifiedByTeacherId;
        this.verifiedByTeacherName = verifiedByTeacherName;
        this.verifiedAt = verifiedAt;
        this.verificationRemark = verificationRemark;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String subjectName;
        private Double selfReportedMarks;
        private Double verifiedMarks;
        private Boolean isVerified;
        private String semester;
        private String verifiedByTeacherId;
        private String verifiedByTeacherName;
        private Instant verifiedAt;
        private String verificationRemark;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder subjectName(String subjectName) {
            this.subjectName = subjectName;
            return this;
        }

        public Builder selfReportedMarks(Double selfReportedMarks) {
            this.selfReportedMarks = selfReportedMarks;
            return this;
        }

        public Builder verifiedMarks(Double verifiedMarks) {
            this.verifiedMarks = verifiedMarks;
            return this;
        }

        public Builder isVerified(Boolean isVerified) {
            this.isVerified = isVerified;
            return this;
        }

        public Builder semester(String semester) {
            this.semester = semester;
            return this;
        }

        public Builder verifiedByTeacherId(String verifiedByTeacherId) {
            this.verifiedByTeacherId = verifiedByTeacherId;
            return this;
        }

        public Builder verifiedByTeacherName(String verifiedByTeacherName) {
            this.verifiedByTeacherName = verifiedByTeacherName;
            return this;
        }

        public Builder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder verificationRemark(String verificationRemark) {
            this.verificationRemark = verificationRemark;
            return this;
        }

        public SubjectMarkResponse build() {
            return new SubjectMarkResponse(id, subjectName, selfReportedMarks, verifiedMarks, isVerified, semester, verifiedByTeacherId, verifiedByTeacherName, verifiedAt, verificationRemark);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getSelfReportedMarks() {
        return selfReportedMarks;
    }

    public void setSelfReportedMarks(Double selfReportedMarks) {
        this.selfReportedMarks = selfReportedMarks;
    }

    public Double getVerifiedMarks() {
        return verifiedMarks;
    }

    public void setVerifiedMarks(Double verifiedMarks) {
        this.verifiedMarks = verifiedMarks;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getVerifiedByTeacherId() {
        return verifiedByTeacherId;
    }

    public void setVerifiedByTeacherId(String verifiedByTeacherId) {
        this.verifiedByTeacherId = verifiedByTeacherId;
    }

    public String getVerifiedByTeacherName() {
        return verifiedByTeacherName;
    }

    public void setVerifiedByTeacherName(String verifiedByTeacherName) {
        this.verifiedByTeacherName = verifiedByTeacherName;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerificationRemark() {
        return verificationRemark;
    }

    public void setVerificationRemark(String verificationRemark) {
        this.verificationRemark = verificationRemark;
    }
}
