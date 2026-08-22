package com.matcher.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_subjects", indexes = {
        @Index(name = "idx_teacher_sub_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_teacher_sub_name", columnList = "subject_name")
})
public class TeacherSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    public TeacherSubject() {
    }

    public TeacherSubject(TeacherProfile teacher, String subjectName) {
        this.teacher = teacher;
        this.subjectName = subjectName;
    }

    public TeacherSubject(Long id, TeacherProfile teacher, String subjectName) {
        this.id = id;
        this.teacher = teacher;
        this.subjectName = subjectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeacherProfile getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherProfile teacher) {
        this.teacher = teacher;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
