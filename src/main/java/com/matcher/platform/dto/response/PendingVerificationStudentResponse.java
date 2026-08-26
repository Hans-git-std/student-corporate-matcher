package com.matcher.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Student with pending self-reported academic marks requiring faculty verification")
public class PendingVerificationStudentResponse {

    @Schema(example = "1")
    private Long studentId;

    @Schema(example = "Alex Morgan")
    private String studentName;

    @Schema(example = "CS-2026-089")
    private String rollNumber;

    @Schema(example = "student@university.edu")
    private String email;

    @Schema(example = "3")
    private int unverifiedCount;

    private List<SubjectMarkResponse> pendingMarks;

    public PendingVerificationStudentResponse() {
    }

    public PendingVerificationStudentResponse(Long studentId, String studentName, String rollNumber, String email, int unverifiedCount, List<SubjectMarkResponse> pendingMarks) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.email = email;
        this.unverifiedCount = unverifiedCount;
        this.pendingMarks = pendingMarks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long studentId;
        private String studentName;
        private String rollNumber;
        private String email;
        private int unverifiedCount;
        private List<SubjectMarkResponse> pendingMarks;

        public Builder studentId(Long studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder studentName(String studentName) {
            this.studentName = studentName;
            return this;
        }

        public Builder rollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder unverifiedCount(int unverifiedCount) {
            this.unverifiedCount = unverifiedCount;
            return this;
        }

        public Builder pendingMarks(List<SubjectMarkResponse> pendingMarks) {
            this.pendingMarks = pendingMarks;
            return this;
        }

        public PendingVerificationStudentResponse build() {
            return new PendingVerificationStudentResponse(studentId, studentName, rollNumber, email, unverifiedCount, pendingMarks);
        }
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUnverifiedCount() {
        return unverifiedCount;
    }

    public void setUnverifiedCount(int unverifiedCount) {
        this.unverifiedCount = unverifiedCount;
    }

    public List<SubjectMarkResponse> getPendingMarks() {
        return pendingMarks;
    }

    public void setPendingMarks(List<SubjectMarkResponse> pendingMarks) {
        this.pendingMarks = pendingMarks;
    }
}
