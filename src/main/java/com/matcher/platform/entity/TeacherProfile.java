package com.matcher.platform.entity;

import com.matcher.platform.entity.enums.ApprovalStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher_profiles", indexes = {
        @Index(name = "idx_teacher_emp_id", columnList = "employee_id", unique = true),
        @Index(name = "idx_teacher_user_id", columnList = "user_id"),
        @Index(name = "idx_teacher_approval_status", columnList = "approval_status")
})
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_id", nullable = false, unique = true, length = 30)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "verified_by_admin_at")
    private Instant verifiedByAdminAt;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeacherSubject> assignedSubjects = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public TeacherProfile() {
    }

    public TeacherProfile(Long id, User user, String employeeId, String fullName, String department, String designation, String phoneNumber, ApprovalStatus approvalStatus, String rejectionReason, Instant verifiedByAdminAt, List<TeacherSubject> assignedSubjects, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
        this.phoneNumber = phoneNumber;
        this.approvalStatus = approvalStatus != null ? approvalStatus : ApprovalStatus.PENDING;
        this.rejectionReason = rejectionReason;
        this.verifiedByAdminAt = verifiedByAdminAt;
        if (assignedSubjects != null) this.assignedSubjects = assignedSubjects;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (approvalStatus == null) approvalStatus = ApprovalStatus.PENDING;
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
        private String employeeId;
        private String fullName;
        private String department;
        private String designation;
        private String phoneNumber;
        private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
        private String rejectionReason;
        private Instant verifiedByAdminAt;
        private List<TeacherSubject> assignedSubjects = new ArrayList<>();
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

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
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

        public Builder assignedSubjects(List<TeacherSubject> assignedSubjects) {
            this.assignedSubjects = assignedSubjects;
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

        public TeacherProfile build() {
            return new TeacherProfile(id, user, employeeId, fullName, department, designation, phoneNumber, approvalStatus, rejectionReason, verifiedByAdminAt, assignedSubjects, createdAt, updatedAt);
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public List<TeacherSubject> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<TeacherSubject> assignedSubjects) {
        this.assignedSubjects = assignedSubjects;
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
