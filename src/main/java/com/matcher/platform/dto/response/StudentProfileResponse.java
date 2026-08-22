package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Full student profile with skills, academic records, and verification status")
public class StudentProfileResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "student@university.edu")
    private String email;

    @Schema(example = "Alex Morgan")
    private String fullName;

    @Schema(example = "CS-2026-089")
    private String rollNumber;

    @Schema(example = "+1234567890")
    private String phoneNumber;

    @Schema(example = "2003-05-15")
    private LocalDate dateOfBirth;

    @Schema(example = "Female")
    private String gender;

    @Schema(example = "123 University Ave, Silicon Valley, CA")
    private String address;

    @Schema(example = "Aspiring backend software engineer")
    private String bio;

    @Schema(example = "https://github.com/alexmorgan")
    private String githubUrl;

    @Schema(example = "https://linkedin.com/in/alexmorgan")
    private String linkedinUrl;

    @Schema(example = "86.4", description = "Calculated aggregate percentage across subjects")
    private Double aggregatePercentage;

    @Schema(example = "true", description = "True if all reported marks have been verified by teachers")
    private Boolean allMarksVerified;

    @Schema(example = "Verification by Teacher is Required", description = "Global remark if verification is pending")
    private String verificationRemark;

    @Schema(description = "List of subject marks")
    private List<SubjectMarkResponse> academicMarks;

    @Schema(description = "List of student skills")
    private List<StudentSkillResponse> skills;

    public StudentProfileResponse() {
    }

    public StudentProfileResponse(Long id, String email, String fullName, String rollNumber, String phoneNumber, LocalDate dateOfBirth, String gender, String address, String bio, String githubUrl, String linkedinUrl, Double aggregatePercentage, Boolean allMarksVerified, String verificationRemark, List<SubjectMarkResponse> academicMarks, List<StudentSkillResponse> skills) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.rollNumber = rollNumber;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.aggregatePercentage = aggregatePercentage;
        this.allMarksVerified = allMarksVerified;
        this.verificationRemark = verificationRemark;
        this.academicMarks = academicMarks;
        this.skills = skills;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String fullName;
        private String rollNumber;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private String bio;
        private String githubUrl;
        private String linkedinUrl;
        private Double aggregatePercentage;
        private Boolean allMarksVerified;
        private String verificationRemark;
        private List<SubjectMarkResponse> academicMarks;
        private List<StudentSkillResponse> skills;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder rollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder githubUrl(String githubUrl) {
            this.githubUrl = githubUrl;
            return this;
        }

        public Builder linkedinUrl(String linkedinUrl) {
            this.linkedinUrl = linkedinUrl;
            return this;
        }

        public Builder aggregatePercentage(Double aggregatePercentage) {
            this.aggregatePercentage = aggregatePercentage;
            return this;
        }

        public Builder allMarksVerified(Boolean allMarksVerified) {
            this.allMarksVerified = allMarksVerified;
            return this;
        }

        public Builder verificationRemark(String verificationRemark) {
            this.verificationRemark = verificationRemark;
            return this;
        }

        public Builder academicMarks(List<SubjectMarkResponse> academicMarks) {
            this.academicMarks = academicMarks;
            return this;
        }

        public Builder skills(List<StudentSkillResponse> skills) {
            this.skills = skills;
            return this;
        }

        public StudentProfileResponse build() {
            return new StudentProfileResponse(id, email, fullName, rollNumber, phoneNumber, dateOfBirth, gender, address, bio, githubUrl, linkedinUrl, aggregatePercentage, allMarksVerified, verificationRemark, academicMarks, skills);
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public Double getAggregatePercentage() {
        return aggregatePercentage;
    }

    public void setAggregatePercentage(Double aggregatePercentage) {
        this.aggregatePercentage = aggregatePercentage;
    }

    public Boolean getAllMarksVerified() {
        return allMarksVerified;
    }

    public void setAllMarksVerified(Boolean allMarksVerified) {
        this.allMarksVerified = allMarksVerified;
    }

    public String getVerificationRemark() {
        return verificationRemark;
    }

    public void setVerificationRemark(String verificationRemark) {
        this.verificationRemark = verificationRemark;
    }

    public List<SubjectMarkResponse> getAcademicMarks() {
        return academicMarks;
    }

    public void setAcademicMarks(List<SubjectMarkResponse> academicMarks) {
        this.academicMarks = academicMarks;
    }

    public List<StudentSkillResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<StudentSkillResponse> skills) {
        this.skills = skills;
    }
}
