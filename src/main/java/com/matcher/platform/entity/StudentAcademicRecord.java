package com.matcher.platform.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "student_academic_records", indexes = {
        @Index(name = "idx_acad_student_id", columnList = "student_id"),
        @Index(name = "idx_acad_subject_name", columnList = "subject_name"),
        @Index(name = "idx_acad_is_verified", columnList = "is_verified")
})
public class StudentAcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "self_reported_marks")
    private Double selfReportedMarks;

    @Column(name = "verified_marks")
    private Double verifiedMarks;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(length = 30)
    private String semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_teacher_id")
    private TeacherProfile verifiedByTeacher;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public StudentAcademicRecord() {
    }

    public StudentAcademicRecord(Long id, StudentProfile student, String subjectName, Double selfReportedMarks, Double verifiedMarks, Boolean isVerified, String semester, TeacherProfile verifiedByTeacher, Instant verifiedAt, String remarks, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.student = student;
        this.subjectName = subjectName;
        this.selfReportedMarks = selfReportedMarks;
        this.verifiedMarks = verifiedMarks;
        this.isVerified = isVerified != null ? isVerified : false;
        this.semester = semester;
        this.verifiedByTeacher = verifiedByTeacher;
        this.verifiedAt = verifiedAt;
        this.remarks = remarks;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Double getEffectiveMark() {
        if (isVerified && verifiedMarks != null) {
            return verifiedMarks;
        }
        return selfReportedMarks != null ? selfReportedMarks : 0.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private StudentProfile student;
        private String subjectName;
        private Double selfReportedMarks;
        private Double verifiedMarks;
        private Boolean isVerified = false;
        private String semester;
        private TeacherProfile verifiedByTeacher;
        private Instant verifiedAt;
        private String remarks;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder student(StudentProfile student) {
            this.student = student;
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

        public Builder verifiedByTeacher(TeacherProfile verifiedByTeacher) {
            this.verifiedByTeacher = verifiedByTeacher;
            return this;
        }

        public Builder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
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

        public StudentAcademicRecord build() {
            return new StudentAcademicRecord(id, student, subjectName, selfReportedMarks, verifiedMarks, isVerified, semester, verifiedByTeacher, verifiedAt, remarks, createdAt, updatedAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public void setStudent(StudentProfile student) {
        this.student = student;
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

    public TeacherProfile getVerifiedByTeacher() {
        return verifiedByTeacher;
    }

    public void setVerifiedByTeacher(TeacherProfile verifiedByTeacher) {
        this.verifiedByTeacher = verifiedByTeacher;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
