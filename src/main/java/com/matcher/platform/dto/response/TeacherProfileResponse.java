package com.matcher.platform.dto.response;

import com.matcher.platform.entity.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Teacher profile details")
public class TeacherProfileResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "turing@faculty.edu")
    private String email;

    @Schema(example = "Dr. Alan Turing")
    private String fullName;

    @Schema(example = "EMP-FAC-1002")
    private String employeeId;

    @Schema(example = "Computer Science & Engineering")
    private String department;

    @Schema(example = "Professor")
    private String designation;

    @Schema(example = "+1987654321")
    private String phoneNumber;

    @Schema(example = "APPROVED")
    private ApprovalStatus approvalStatus;

    private String rejectionReason;

    private Instant verifiedByAdminAt;

    private Instant createdAt;

    @Schema(description = "Assigned subjects for instruction/verification")
    private List<String> assignedSubjects;

    public TeacherProfileResponse() {
    }

    public TeacherProfileResponse(Long id, String email, String fullName, String employeeId, String department, String designation, String phoneNumber, ApprovalStatus approvalStatus, String rejectionReason, Instant verifiedByAdminAt, Instant createdAt, List<String> assignedSubjects) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.phoneNumber = phoneNumber;
        this.approvalStatus = approvalStatus;
        this.rejectionReason = rejectionReason;
        this.verifiedByAdminAt = verifiedByAdminAt;
        this.createdAt = createdAt;
        this.assignedSubjects = assignedSubjects;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String fullName;
        private String employeeId;
        private String department;
        private String designation;
        private String phoneNumber;
        private ApprovalStatus approvalStatus;
        private String rejectionReason;
        private Instant verifiedByAdminAt;
        private Instant createdAt;
        private List<String> assignedSubjects;

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

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder designation(String designation) {
            this.designation = designation;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder approvalStatus(ApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public Builder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public Builder verifiedByAdminAt(Instant verifiedByAdminAt) {
            this.verifiedByAdminAt = verifiedByAdminAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder assignedSubjects(List<String> assignedSubjects) {
            this.assignedSubjects = assignedSubjects;
            return this;
        }

        public TeacherProfileResponse build() {
            return new TeacherProfileResponse(id, email, fullName, employeeId, department, designation, phoneNumber, approvalStatus, rejectionReason, verifiedByAdminAt, createdAt, assignedSubjects);
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getVerifiedByAdminAt() {
        return verifiedByAdminAt;
    }

    public void setVerifiedByAdminAt(Instant verifiedByAdminAt) {
        this.verifiedByAdminAt = verifiedByAdminAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<String> assignedSubjects) {
        this.assignedSubjects = assignedSubjects;
    }
}
