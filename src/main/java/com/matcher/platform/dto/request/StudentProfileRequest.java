package com.matcher.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request payload to create or update student profile")
public class StudentProfileRequest {

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(example = "Alex Morgan")
    private String fullName;

    @NotBlank(message = "Roll number is mandatory")
    @Pattern(regexp = "^[A-Z0-9-]{4,20}$", message = "Roll number must be alphanumeric with optional dashes (4-20 chars)")
    @Schema(example = "CS-2026-089")
    private String rollNumber;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid 10-15 digits")
    @Schema(example = "+1234567890")
    private String phoneNumber;

    @Schema(example = "2003-05-15", description = "Date of Birth (YYYY-MM-DD)")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    @Schema(example = "Female")
    private String gender;

    @Size(max = 255, message = "Address line must not exceed 255 characters")
    @Schema(example = "123 University Ave, Silicon Valley, CA")
    private String address;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    @Schema(example = "Aspiring backend software engineer with passion for distributed systems.")
    private String bio;

    @Schema(example = "https://github.com/alexmorgan")
    private String githubUrl;

    @Schema(example = "https://linkedin.com/in/alexmorgan")
    private String linkedinUrl;

    public StudentProfileRequest() {
    }

    public StudentProfileRequest(String fullName, String rollNumber, String phoneNumber, LocalDate dateOfBirth, String gender, String address, String bio, String githubUrl, String linkedinUrl) {
        this.fullName = fullName;
        this.rollNumber = rollNumber;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fullName;
        private String rollNumber;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private String gender;
        private String address;
        private String bio;
        private String githubUrl;
        private String linkedinUrl;

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

        public StudentProfileRequest build() {
            return new StudentProfileRequest(fullName, rollNumber, phoneNumber, dateOfBirth, gender, address, bio, githubUrl, linkedinUrl);
        }
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
}
